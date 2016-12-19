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
import utils.DateHelper
import wash._

import io.megam.util.Time
import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._
import com.websudos.phantom.iteratee.Iteratee
import controllers.stack.ImplicitJsonFormats

/**
 * @author rajesh
 *
 */
case class EventsStorageInput(account_id: String, created_at: String, event_type: String, data: KeyValueList)

case class EventsStorageResult(
  id: String,
  account_id: String,
  created_at: DateTime,
  event_type: String,
  data: models.tosca.KeyValueList,
  json_claz: String
)

object EventsStorageResult {
  def apply(id: String, account_id: String, created_at: DateTime, event_type: String, data: models.tosca.KeyValueList) = new EventsStorageResult(id, account_id, created_at, event_type, data, "Megam::EventsStorage")
}

sealed class EventsStorageSacks extends CassandraTable[EventsStorageSacks, EventsStorageResult] with ImplicitJsonFormats {

  object account_id extends StringColumn(this) with PartitionKey[String]
  object created_at extends DateTimeColumn(this) with PrimaryKey[DateTime]
  object event_type extends StringColumn(this) with PrimaryKey[String]

  object data extends JsonListColumn[EventsStorageSacks, EventsStorageResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object id extends StringColumn(this)
  object json_claz extends StringColumn(this)

  def fromRow(row: Row): EventsStorageResult = {
    EventsStorageResult(
      id(row),
      account_id(row),
      created_at(row),
      event_type(row),
      data(row),
      json_claz(row))
  }
}

abstract class ConcreteEventsStorage extends EventsStorageSacks with RootConnector {
  override lazy val tableName = "events_for_storages"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(evt: EventsStorageResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, evt.id)
      .value(_.account_id, evt.account_id)
      .value(_.created_at, evt.created_at)
      .value(_.event_type, evt.event_type)
      .value(_.data, evt.data)
      .value(_.json_claz, evt.json_claz)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(email: String, limit: String): ValidationNel[Throwable, Seq[EventsStorageResult]] = {
    val count = (if (limit == "0")  "10" else  limit)
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
  } yield {
    val bvalue = Set(email)
    val json = new EventsStorageResult("",email, DateHelper.now(eventsstorage.created_at), eventsstorage.event_type, eventsstorage.data, "Megam::EventsStorage")
    json
  }
}

private def EventsStorageSack(email: String, input: String): ValidationNel[Throwable, EventsStorageResult] = {
  val EventsStorageInput: ValidationNel[Throwable, EventsStorageInput] = (Validation.fromTryCatchThrowable[EventsStorageInput, Throwable] {
    parse(input).extract[EventsStorageInput]
  } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

  for {
    store <- EventsStorageInput
    uir <- (UID("EST").get leftMap { ut: NonEmptyList[Throwable] => ut })
  } yield {

    val bvalue = Set(email)
    val json = new EventsStorageResult(uir.get._1 + uir.get._2, email, DateHelper.now(store.created_at), store.event_type, store.data, "Megam::EventsStorage")
    json
  }
}


def create(email: String, input: String): ValidationNel[Throwable, Option[EventsStorageResult]] = {
  for {
    wa <- (EventsStorageSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
    set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
  } yield {
    play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "EventsStorage.created success", Console.RESET))
    wa.some
  }
}
  def findByEmail(accountID: String, limit: String): ValidationNel[Throwable, Seq[EventsStorageResult]] = {
    (listRecords(accountID, limit) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsStorageResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[EventsStorageResult]](nm).toValidationNel
       else   Validation.failure[Throwable, Seq[EventsStorageResult]](new ResourceItemNotFound(accountID, "EventsStorage = nothing found.")).toValidationNel

    }
  }
  def IndexEmail(accountID: String): ValidationNel[Throwable, Seq[EventsStorageResult]] = {
    (indexRecords(accountID) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsStorageResult] =>
      if (!nm.isEmpty) Validation.success[Throwable, Seq[EventsStorageResult]](nm).toValidationNel
      else Validation.failure[Throwable, Seq[EventsStorageResult]](new ResourceItemNotFound(accountID, "EventsStorage = nothing found.")).toValidationNel
    }
  }
}
