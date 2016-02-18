/*
** Copyright [2013-2016] [Megam Systems]
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package models.base

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import models.json._
import models.base._
import db._
import cache._
import app.MConfig
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._
import scalaz.Validation
import scalaz.Validation.FlatMap._

import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import io.megam.common.riak.GunnySack
import io.megam.util.Time
import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
//import com.twitter.util.{ Future, Await }
import scala.concurrent.Await
import scala.concurrent.duration._

//import com.twitter.conversions.time._
import org.joda.time.DateTime

import com.websudos.phantom.dsl._
import com.websudos.phantom.iteratee.Iteratee
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }

/**
 *
 * @author morpheyesh
 */

case class SshKeysInput(name: String, privatekey: String, publickey: String) {
  val json = "{\"name\":\"" + name + "\",\"privatekey\":\"" + privatekey + "\",\"publickey\":\"" + publickey + "\"}"
}


case class SshKeysResult(
  id: String,
  org_id: String,
  name: String,
  privatekey: String,
  publickey: String,
  json_claz: String,
  created_at: String) {}



sealed class SshKeysT extends CassandraTable[SshKeysT, SshKeysResult] {

  object id extends StringColumn(this) with PrimaryKey[String]
  object org_id extends StringColumn(this) with PartitionKey[String]
  object name extends StringColumn(this)
  object privatekey extends StringColumn(this)
  object publickey extends StringColumn(this)
  object json_claz extends StringColumn(this)
  object created_at extends StringColumn(this)

  override def fromRow(r: Row): SshKeysResult = {
    SshKeysResult(
      id(r),
      org_id(r),
      name(r),
      privatekey(r),
      publickey(r),
      json_claz(r),
      created_at(r))
  }
}

/*
 *   This class talks to scylla and performs the actions
 */

abstract class ConcreteOrg extends SshKeysT with ScyllaConnector {

  override lazy val tableName = "sshkeys"

  def insertNewRecord(sk: SshKeysResult): ResultSet = {
    val res = insert.value(_.id, sk.id)
      .value(_.org_id, sk.org_id)
      .value(_.name, sk.name)
      .value(_.privatekey, sk.privatekey)
      .value(_.publickey, sk.publickey)
      .value(_.json_claz, sk.json_claz)
      .value(_.created_at, sk.created_at)
      .future()
    Await.result(res, 5.seconds)
  }
/*
 * Lists all the record
 */
  def listRecords(org_id: String): ValidationNel[Throwable, Seq[SshKeysResult]] = {
    val resp = select.allowFiltering().where(_.org_id eqs org_id).fetch()
    (Await.result(resp, 5.seconds)).successNel
  }
}


object SshKeys extends ConcreteOrg {

  implicit val formats = DefaultFormats

  private def sshNel(input: String): ValidationNel[Throwable, SshKeysInput] = {
    (Validation.fromTryCatchThrowable[SshKeysInput, Throwable] {
      parse(input).extract[SshKeysInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
  }

  private def SshKeysSet(id: String, org_id: String, c: SshKeysInput): ValidationNel[Throwable, SshKeysResult] = {
    (Validation.fromTryCatchThrowable[SshKeysResult, Throwable] {
      SshKeysResult(id, org_id, c.name, c.privatekey, c.publickey, "Megam::SshKeys", Time.now.toString)
    } leftMap { t: Throwable => new MalformedBodyError(c.json, t.getMessage) }).toValidationNel
  }

  def create(authBag: Option[io.megam.auth.stack.AuthBag], input: String): ValidationNel[Throwable, SshKeysResult] = {
    for {
      c <- sshNel(input)
      uir <- (UID("SSH").get leftMap { u: NonEmptyList[Throwable] => u })
      sk <- SshKeysSet(uir.get._1 + uir.get._2, authBag.get.org_id, c)
    } yield {
      insertNewRecord(sk)
      sk
    }
  }

  def findByOrgId(authBag: Option[io.megam.auth.stack.AuthBag]): ValidationNel[Throwable, Seq[SshKeysResult]] = {
    (listRecords(authBag.get.org_id) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(authBag.get.email, "Sshkeys = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[SshKeysResult] =>
        Validation.success[Throwable, Seq[SshKeysResult]](nm).toValidationNel
    }
 }
}
