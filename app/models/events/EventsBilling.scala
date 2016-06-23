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

case class EventsBillingInput(account_id: String, created_at: String, assembly_id: String, event_type: String, data: KeyValueList) {
}
case class EventsBillingResult(
  account_id: String,
  created_at: String,
  assembly_id: String,
  event_type: String,
  data: models.tosca.KeyValueList,
  json_claz: String
) {
}

object EventsBillingResult {
  def apply(account_id: String, created_at: String, assembly_id: String, event_type: String, data: models.tosca.KeyValueList) = new EventsBillingResult(account_id, created_at, assembly_id, event_type, data, "Megam::EventsBilling")
}

sealed class EventsBillingSacks extends CassandraTable[EventsBillingSacks, EventsBillingResult] {

  implicit val formats = DefaultFormats

  object account_id extends StringColumn(this) with PartitionKey[String]
  object created_at extends StringColumn(this) with PrimaryKey[String]
  object assembly_id extends StringColumn(this) with PrimaryKey[String]
  object event_type extends StringColumn(this) with PrimaryKey[String]

  object data extends JsonListColumn[EventsBillingSacks, EventsBillingResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object json_claz extends StringColumn(this)

  def fromRow(row: Row): EventsBillingResult = {
    EventsBillingResult(
      account_id(row),
      created_at(row),
      assembly_id(row),
      event_type(row),
      data(row),
      json_claz(row))
  }
}

abstract class ConcreteEventsBilling extends EventsBillingSacks with RootConnector {
  // you can even rename the table in the schema to whatever you like.
  override lazy val tableName = "events_for_billings"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(evt: EventsBillingResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.account_id, evt.account_id)
      .value(_.created_at, evt.created_at)
      .value(_.assembly_id, evt.assembly_id)
      .value(_.event_type, evt.event_type)
      .value(_.data, evt.data)
      .value(_.json_claz, evt.json_claz)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(email: String, limit: String): ValidationNel[Throwable, Seq[EventsBillingResult]] = {
      var count = " "
     if (limit == "0") {
        count = "10"
     } else {
       count = limit
     }
    val res = select.where(_.account_id eqs email).limit(count.toInt).fetch()
    Await.result(res, 5.seconds).successNel
    }


  def indexRecords(email: String): ValidationNel[Throwable, Seq[EventsBillingResult]] = {
    val res = select.where(_.account_id eqs email).fetch()
    Await.result(res, 5.seconds).successNel

  }
  def getRecords(created_at: String,assembly_id: String, limit: String): ValidationNel[Throwable, Seq[EventsBillingResult]] = {
    var count = ""
     if (limit == "0") {
     count = "10"
     } else {
     count = limit
     }
    val res = select.allowFiltering().where(_.created_at eqs created_at).and(_.assembly_id eqs assembly_id).limit(count.toInt).fetch()
    Await.result(res, 5.seconds).successNel
     }
}

object EventsBilling extends ConcreteEventsBilling {
private def mkEventsBillingSack(email: String, input: String): ValidationNel[Throwable, EventsBillingResult] = {
  val EventsBillingInput: ValidationNel[Throwable, EventsBillingInput] = (Validation.fromTryCatchThrowable[EventsBillingInput, Throwable] {
    parse(input).extract[EventsBillingInput]
  } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
  for {
    eventsbilling <- EventsBillingInput
    //uir <- (UID("sps").get leftMap { ut: NonEmptyList[Throwable] => ut })
  } yield {
    val bvalue = Set(email)
    val json = new EventsBillingResult(email,eventsbilling.created_at,eventsbilling.assembly_id,eventsbilling.event_type,eventsbilling.data, "Megam::EventsBilling")
    json
  }
}

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email,
   * the csarnames are listed on the index (account.id) in bucket `CSARs`.
   * Using a "csarname" as key, return a list of ValidationNel[List[CSARResult]]
   * Takes an email, and returns a Future[ValidationNel, List[Option[CSARResult]]]
   */
  def findByEmail(accountID: String, limit: String): ValidationNel[Throwable, Seq[EventsBillingResult]] = {
    (listRecords(accountID, limit) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsBillingResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[EventsBillingResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[EventsBillingResult]](new ResourceItemNotFound(accountID, "EventsBilling = nothing found.")).toValidationNel
    }
  }
  def IndexEmail(accountID: String): ValidationNel[Throwable, Seq[EventsBillingResult]] = {
    (indexRecords(accountID) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsBillingResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[EventsBillingResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[EventsBillingResult]](new ResourceItemNotFound(accountID, "EventsBilling = nothing found.")).toValidationNel
    }

  }

  def findById(email: String, input: String, limit: String): ValidationNel[Throwable, Seq[EventsBillingResult]] = {
   (mkEventsBillingSack(email, input) leftMap { err: NonEmptyList[Throwable] => err
   }).flatMap {ws: EventsBillingResult =>
    (getRecords(ws.created_at,ws.assembly_id, limit) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(ws.assembly_id, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsBillingResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[EventsBillingResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[EventsBillingResult]](new ResourceItemNotFound(ws.assembly_id, "EventsBilling = nothing found.")).toValidationNel
    }

  }
}

}
