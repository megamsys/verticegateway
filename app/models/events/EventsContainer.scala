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
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

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

case class EventsContainerInput(
  account_id: String,
  created_at: String,
  assembly_id: String,
  event_type: String,
  data: KeyValueList) {
}

case class EventsContainerResult(
  id: String,
  account_id: String,
  created_at: DateTime,
  assembly_id: String,
  event_type: String,
  data: models.tosca.KeyValueList,
  json_claz: String) {
}

case class EventsContainerReturnResult(
  id: String,
  account_id: String,
  created_at: String,
  assembly_id: String,
  event_type: String,
  data: models.tosca.KeyValueList,
  json_claz: String) {}

object EventsContainerResult {
  def apply(id: String, account_id: String, created_at: DateTime, assembly_id: String, event_type: String, data: models.tosca.KeyValueList) = new EventsContainerResult(id, account_id, created_at, assembly_id, event_type, data, "Megam::EventsContainer")
}

sealed class EventsContainerSacks extends CassandraTable[EventsContainerSacks, EventsContainerResult] {

  implicit val formats = DefaultFormats

  object id extends StringColumn(this)
  object account_id extends StringColumn(this) with PartitionKey[String]
  object created_at extends DateTimeColumn(this) with PrimaryKey[DateTime]
  object assembly_id extends StringColumn(this) with PrimaryKey[String]
  object event_type extends StringColumn(this) with PrimaryKey[String]

  object data extends JsonListColumn[EventsContainerSacks, EventsContainerResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object json_claz extends StringColumn(this)

  def fromRow(row: Row): EventsContainerResult = {
    EventsContainerResult(
      id(row),
      account_id(row),
      created_at(row),
      assembly_id(row),
      event_type(row),
      data(row),
      json_claz(row))
  }
}

abstract class ConcreteEventsContainer extends EventsContainerSacks with RootConnector {
  // you can even rename the table in the schema to whatever you like.
  override lazy val tableName = "events_for_containers"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(evt: EventsContainerResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, evt.id)
      .value(_.account_id, evt.account_id)
      .value(_.created_at, evt.created_at)
      .value(_.assembly_id, evt.assembly_id)
      .value(_.event_type, evt.event_type)
      .value(_.data, evt.data)
      .value(_.json_claz, evt.json_claz)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(email: String, limit: String): ValidationNel[Throwable, Seq[EventsContainerResult]] = {
    var count = " "
    if (limit == "0") {
      count = "10"
    } else {
      count = limit
    }
    val res = select.where(_.account_id eqs email).limit(count.toInt).fetch()
    Await.result(res, 5.seconds).successNel
  }

  def indexRecords(email: String): ValidationNel[Throwable, Seq[EventsContainerResult]] = {
    val res = select.where(_.account_id eqs email).fetch()
    Await.result(res, 5.seconds).successNel

  }
  def getRecords(created_at: DateTime, assembly_id: String, limit: String): ValidationNel[Throwable, Seq[EventsContainerResult]] = {
    var count = ""
    if (limit == "0") {
      count = "10"
    } else {
      count = limit
    }
    val times = getTimes(created_at)
    val res = select.allowFiltering().where(_.created_at gte times._1).and(_.created_at lte times._2).and(_.assembly_id eqs assembly_id).limit(count.toInt).fetch()
    Await.result(res, 5.seconds).successNel
  }

def getTimes(created_at: DateTime) = {
  new Tuple2(
    created_at,
    DateTime.parse(Time.now.toString, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z")))
 }
}

object EventsContainer extends ConcreteEventsContainer {

def generateCreatedAt(created_at: String): DateTime = {
  if (created_at == "" || created_at == null) {
    return DateTime.parse(Time.now.toString, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z")).minusMinutes(10)
  } else {
    return DateTime.parse(created_at, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z"))
  }
}

  private def mkEventsContainerSack(email: String, input: String): ValidationNel[Throwable, EventsContainerResult] = {
    val EventsContainerInput: ValidationNel[Throwable, EventsContainerInput] = (Validation.fromTryCatchThrowable[EventsContainerInput, Throwable] {
      parse(input).extract[EventsContainerInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
    for {
      eventscontainer <- EventsContainerInput
      //uir <- (UID("sps").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(email)
      val json = new EventsContainerResult("", email, generateCreatedAt(eventscontainer.created_at), eventscontainer.assembly_id, eventscontainer.event_type, eventscontainer.data, "Megam::EventsContainer")
      json
    }
  }

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email,
   * the csarnames are listed on the index (account.id) in bucket `CSARs`.
   * Using a "csarname" as key, return a list of ValidationNel[List[CSARResult]]
   * Takes an email, and returns a Future[ValidationNel, List[Option[CSARResult]]]
   */
  def findByEmail(accountID: String, limit: String): ValidationNel[Throwable, Seq[EventsContainerReturnResult]] = {
    (listRecords(accountID, limit) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsContainerResult] =>
    if (!nm.isEmpty) {
      val res = nm.map {
        evr ⇒ new EventsContainerReturnResult(evr.id, evr.account_id, evr.created_at.toString(), evr.assembly_id, evr.event_type, evr.data, evr.json_claz)
      }
      Validation.success[Throwable, Seq[EventsContainerReturnResult]](res).toValidationNel
    } else {
        Validation.failure[Throwable, Seq[EventsContainerReturnResult]](new ResourceItemNotFound(accountID, "EventsContainer = nothing found.")).toValidationNel
        }
    }
  }
  def IndexEmail(accountID: String): ValidationNel[Throwable, Seq[EventsContainerReturnResult]] = {
    (indexRecords(accountID) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsContainerResult] =>
    if (!nm.isEmpty) {
      val res = nm.map {
        evr ⇒ new EventsContainerReturnResult(evr.id, evr.account_id, evr.created_at.toString(), evr.assembly_id, evr.event_type, evr.data, evr.json_claz)
      }
      Validation.success[Throwable, Seq[EventsContainerReturnResult]](res).toValidationNel
    } else {
        Validation.failure[Throwable, Seq[EventsContainerReturnResult]](new ResourceItemNotFound(accountID, "EventsContainer = nothing found.")).toValidationNel
        }
    }

  }

  def findById(email: String, input: String, limit: String): ValidationNel[Throwable, Seq[EventsContainerReturnResult]] = {
    (mkEventsContainerSack(email, input) leftMap { err: NonEmptyList[Throwable] => err
    }).flatMap { ws: EventsContainerResult =>
      (getRecords(ws.created_at, ws.assembly_id, limit) leftMap { t: NonEmptyList[Throwable] =>
        new ResourceItemNotFound(ws.assembly_id, "Events = nothing found.")
      }).toValidationNel.flatMap { nm: Seq[EventsContainerResult] =>
        if (!nm.isEmpty) {
          val res = nm.map {
            evr ⇒ new EventsContainerReturnResult(evr.id, evr.account_id, evr.created_at.toString(), evr.assembly_id, evr.event_type, evr.data, evr.json_claz)
          }
          Validation.success[Throwable, Seq[EventsContainerReturnResult]](res).toValidationNel
        } else {
          Validation.failure[Throwable, Seq[EventsContainerReturnResult]](new ResourceItemNotFound(ws.assembly_id, "EventsContainer = nothing found.")).toValidationNel
          }
      }

    }
  }

}
