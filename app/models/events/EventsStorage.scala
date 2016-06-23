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
 * @author rajesh
 *
 */

case class EventsStorageInput(account_id: String, created_at: String, event_type: String, data: KeyValueList) {
}
case class EventsStorageResult(
  account_id: String,
  created_at: String,
  event_type: String,
  data: models.tosca.KeyValueList,
  json_claz: String
) {
}

object EventsStorageResult {
  def apply(account_id: String, created_at: String, event_type: String, data: models.tosca.KeyValueList) = new EventsStorageResult(account_id, created_at, event_type, data, "Megam::EventsStorage")
}

sealed class EventsStorageSacks extends CassandraTable[EventsStorageSacks, EventsStorageResult] {

  implicit val formats = DefaultFormats

  object account_id extends StringColumn(this) with PartitionKey[String]
  object created_at extends StringColumn(this) with PrimaryKey[String]
  object event_type extends StringColumn(this) with PrimaryKey[String]

  object data extends JsonListColumn[EventsStorageSacks, EventsStorageResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object json_claz extends StringColumn(this)

  def fromRow(row: Row): EventsStorageResult = {
    EventsStorageResult(
      account_id(row),
      created_at(row),
      event_type(row),
      data(row),
      json_claz(row))
  }
}

abstract class ConcreteEventsStorage extends EventsStorageSacks with RootConnector {
  // you can even rename the table in the schema to whatever you like.
  override lazy val tableName = "events_for_storages"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(evt: EventsStorageResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.account_id, evt.account_id)
      .value(_.created_at, evt.created_at)
      .value(_.event_type, evt.event_type)
      .value(_.data, evt.data)
      .value(_.json_claz, evt.json_claz)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(email: String, limit: String): ValidationNel[Throwable, Seq[EventsStorageResult]] = {
      var count = " "
     if (limit == "0") {
        count = "10"
     } else {
       count = limit
     }
    val res = select.where(_.account_id eqs email).limit(count.toInt).fetch()
    Await.result(res, 5.seconds).successNel
    }


  def indexRecords(email: String): ValidationNel[Throwable, Seq[EventsStorageResult]] = {
    val res = select.where(_.account_id eqs email).fetch()
    Await.result(res, 5.seconds).successNel
  }
}

object EventsStorage extends ConcreteEventsStorage {
private def mkEventsStorageSack(email: String, input: String): ValidationNel[Throwable, EventsStorageResult] = {
  val EventsStorageInput: ValidationNel[Throwable, EventsStorageInput] = (Validation.fromTryCatchThrowable[EventsStorageInput, Throwable] {
    parse(input).extract[EventsStorageInput]
  } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
  for {
    eventsstorage <- EventsStorageInput
    //uir <- (UID("sps").get leftMap { ut: NonEmptyList[Throwable] => ut })
  } yield {
    val bvalue = Set(email)
    val json = new EventsStorageResult(email, eventsstorage.created_at, eventsstorage.event_type, eventsstorage.data, "Megam::EventsStorage")
    json
  }
}

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email,
   * the csarnames are listed on the index (account.id) in bucket `CSARs`.
   * Using a "csarname" as key, return a list of ValidationNel[List[CSARResult]]
   * Takes an email, and returns a Future[ValidationNel, List[Option[CSARResult]]]
   */
  def findByEmail(accountID: String, limit: String): ValidationNel[Throwable, Seq[EventsStorageResult]] = {
    (listRecords(accountID, limit) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsStorageResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[EventsStorageResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[EventsStorageResult]](new ResourceItemNotFound(accountID, "EventsStorage = nothing found.")).toValidationNel
    }
  }
  def IndexEmail(accountID: String): ValidationNel[Throwable, Seq[EventsStorageResult]] = {
    (indexRecords(accountID) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsStorageResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[EventsStorageResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[EventsStorageResult]](new ResourceItemNotFound(accountID, "EventsStorage = nothing found.")).toValidationNel
    }
  }
}
