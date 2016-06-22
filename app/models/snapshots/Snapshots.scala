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

package models.snapshots

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import cache._
import db._
import models.tosca._
import models.json.tosca._
import models.json.tosca.carton._
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._
import app.MConfig
import models.base._
import wash._

import io.megam.util.Time
import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._
import com.websudos.phantom.iteratee.Iteratee

/**
 * @author ranjitha
 *
 */
case class SnapshotsInput( asm_id: String, org_id: String, account_id: String, name: String) {
}
case class SnapshotsResult(
  snap_id: String,
  asm_id:  String,
  org_id: String,
  account_id: String,
  name:   String,
  json_claz: String,
  created_at: String) {
}

object SnapshotsResult {
  def apply(snap_id: String, asm_id: String, org_id: String, account_id: String, name: String) = new SnapshotsResult(snap_id, asm_id, org_id, account_id, name, "Megam::Snapshots", Time.now.toString)
}

sealed class SnapshotsSacks extends CassandraTable[SnapshotsSacks, SnapshotsResult] {

  implicit val formats = DefaultFormats

  object snap_id extends StringColumn(this) with  PartitionKey[String]
  object asm_id extends StringColumn(this) with PrimaryKey[String]
  object org_id extends StringColumn(this)
  object account_id extends StringColumn(this) with PrimaryKey[String]
  object name extends StringColumn(this)
  object created_at extends StringColumn(this)
  object json_claz extends StringColumn(this)

  def fromRow(row: Row): SnapshotsResult = {
    SnapshotsResult(
      snap_id(row),
      asm_id(row),
      org_id(row),
      account_id(row),
      name(row),
      json_claz(row),
      created_at(row))
  }
}

abstract class ConcreteSnapshots extends SnapshotsSacks with RootConnector {
  // you can even rename the table in the schema to whatever you like.
  override lazy val tableName = "snapshots"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(sps: SnapshotsResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.snap_id, sps.snap_id)
      .value(_.asm_id, sps.asm_id)
      .value(_.org_id, sps.org_id)
      .value(_.account_id, sps.account_id)
      .value(_.name, sps.name)
      .value(_.json_claz, sps.json_claz)
      .value(_.created_at, sps.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(email: String): ValidationNel[Throwable, Seq[SnapshotsResult]] = {
    val res = select.allowFiltering().where(_.account_id eqs email).fetch()
    Await.result(res, 5.seconds).successNel
  }
  def getRecords(assembly_id: String, email: String): ValidationNel[Throwable, Seq[SnapshotsResult]] = {
    val res = select.allowFiltering().where(_.account_id eqs email).and(_.asm_id eqs assembly_id).fetch()
    Await.result(res, 5.seconds).successNel
  }

}

object Snapshots extends ConcreteSnapshots {

/**
 * A private method which chains computation to make GunnySack when provided with an input json, email.
 * parses the json, and converts it to eventsinput, if there is an error during parsing, a MalformedBodyError is sent back.
 * After that flatMap on its success and the account id information is looked up.
 * If the account id is looked up successfully, then yield the GunnySack object.
 */
private def mkSnapshotsSack(email: String, input: String): ValidationNel[Throwable, SnapshotsResult] = {
  val snapshotsInput: ValidationNel[Throwable, SnapshotsInput] = (Validation.fromTryCatchThrowable[SnapshotsInput, Throwable] {
    parse(input).extract[SnapshotsInput]
  } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

  for {
    snap <- snapshotsInput
    uir <- (UID("sps").get leftMap { ut: NonEmptyList[Throwable] => ut })
  } yield {

    val bvalue = Set(email)
    val json = new SnapshotsResult(uir.get._1 + uir.get._2, snap.asm_id, snap.org_id, email, snap.name, "Megam::Snapshots", Time.now.toString)
    json
  }
}

/*
 * create new snapshot for the user.
 *
 */
def create(email: String, input: String): ValidationNel[Throwable, Option[SnapshotsResult]] = {
  for {
    wa <- (mkSnapshotsSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
    set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
  } yield {
    play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Snapshots.created success", Console.RESET))
    wa.some
  }
}

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email,
   * the csarnames are listed on the index (account.id) in bucket `CSARs`.
   * Using a "csarname" as key, return a list of ValidationNel[List[CSARResult]]
   * Takes an email, and returns a Future[ValidationNel, List[Option[CSARResult]]]
   */
  def findByEmail(accountID: String): ValidationNel[Throwable, Seq[SnapshotsResult]] = {
    (listRecords(accountID) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Snapshots = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[SnapshotsResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[SnapshotsResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[SnapshotsResult]](new ResourceItemNotFound(accountID, "Snapshots = nothing found.")).toValidationNel
    }

  }

  def findById(assemblyID: String, email: String): ValidationNel[Throwable, Seq[SnapshotsResult]] = {
    (getRecords(assemblyID, email) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(assemblyID, "Snapshots = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[SnapshotsResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[SnapshotsResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[SnapshotsResult]](new ResourceItemNotFound(assemblyID, "Snapshots = nothing found.")).toValidationNel
    }

  }

}
