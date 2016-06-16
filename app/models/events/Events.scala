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

package models.events

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
case class EventsInput(account_id: String, assembly_id: String, event_type: String, data: KeyValueList, read_status: String) {
}
case class EventsResult(

  account_id: String,
  assembly_id: String,
  event_type: String,
  data: models.tosca.KeyValueList,
  json_claz: String,
  read_status: String,
  created_at: String) {
}

object EventsResult {
  def apply(account_id: String, assembly_id: String, event_type: String, data: models.tosca.KeyValueList, read_status: String) = new EventsResult(account_id, assembly_id, event_type, data, read_status, "Megam::Events", Time.now.toString)
}

sealed class EventsSacks extends CassandraTable[EventsSacks, EventsResult] {

  implicit val formats = DefaultFormats

  object account_id extends StringColumn(this) with PartitionKey[String]
  object assembly_id extends StringColumn(this) with PrimaryKey[String]
  object event_type extends StringColumn(this) with PrimaryKey[String]
  object read_status extends StringColumn(this)
  object created_at extends StringColumn(this) with PrimaryKey[String]
  object data extends JsonListColumn[EventsSacks, EventsResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object json_claz extends StringColumn(this)

  def fromRow(row: Row): EventsResult = {
    EventsResult(
      account_id(row),
      assembly_id(row),
      event_type(row),
      data(row),
      json_claz(row),
      read_status(row),
      created_at(row))
  }
}

abstract class ConcreteEvents extends EventsSacks with RootConnector {
  // you can even rename the table in the schema to whatever you like.
  override lazy val tableName = "events"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(evt: EventsResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.account_id, evt.account_id)
      .value(_.assembly_id, evt.assembly_id)
      .value(_.event_type, evt.event_type)
      .value(_.data, evt.data)
      .value(_.json_claz, evt.json_claz)
      .value(_.read_status, evt.read_status)
      .value(_.created_at, evt.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }
  val end = new DateTime()
  def listRecords(email: String, limit: String): ValidationNel[Throwable, Seq[EventsResult]] = {
    val res = select.where(_.account_id eqs email).limit(limit.toInt).fetch()
    Await.result(res, 5.seconds).successNel
  }
  def getRecords(assembly_id: String): ValidationNel[Throwable, Seq[EventsResult]] = {
    val res = select.allowFiltering().where(_.assembly_id eqs assembly_id).fetch()
    Await.result(res, 5.seconds).successNel
  }

}

object Events extends ConcreteEvents {

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email,
   * the csarnames are listed on the index (account.id) in bucket `CSARs`.
   * Using a "csarname" as key, return a list of ValidationNel[List[CSARResult]]
   * Takes an email, and returns a Future[ValidationNel, List[Option[CSARResult]]]
   */
  def findByEmail(accountID: String, limit: String): ValidationNel[Throwable, Seq[EventsResult]] = {
    (listRecords(accountID, limit) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[EventsResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[EventsResult]](new ResourceItemNotFound(accountID, "Events = nothing found.")).toValidationNel
    }

  }

  def findById(assemblyID: String): ValidationNel[Throwable, Seq[EventsResult]] = {
    (getRecords(assemblyID) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(assemblyID, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[EventsResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[EventsResult]](new ResourceItemNotFound(assemblyID, "Events = nothing found.")).toValidationNel
    }

  }

}
