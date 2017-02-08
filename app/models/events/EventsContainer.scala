package models.events

import scalaz._
import Scalaz._
import scalaz.effect.IO
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

case class EventsContainerInput(
  account_id: String,
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

object EventsContainerResult {
  def apply(id: String, account_id: String, created_at: DateTime, assembly_id: String, event_type: String, data: models.tosca.KeyValueList) = new EventsContainerResult(id, account_id, created_at, assembly_id, event_type, data, "Megam::EventsContainer")
}

sealed class EventsContainerSacks extends CassandraTable[EventsContainerSacks, EventsContainerResult] with ImplicitJsonFormats {

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
  def getRecords(email: String, created_at: DateTime, assembly_id: String, limit: String): ValidationNel[Throwable, Seq[EventsContainerResult]] = {
    val count = (if (limit == "0")  "10" else  limit)

    val times = DateHelper.toTimeRange(created_at)

    val res = select.allowFiltering().where(_.created_at gte times._1).and(_.created_at lte times._2).and(_.assembly_id eqs assembly_id).limit(count.toInt).fetch()
    Await.result(res, 5.seconds).successNel
  }

  def deleteRecords(email: String): ValidationNel[Throwable, ResultSet] = {
    val res = delete.where(_.account_id eqs email).future()
    Await.result(res, 5.seconds).successNel
  }

  def deleteRecordsByAssembly(id: String, email: String): ValidationNel[Throwable, ResultSet] = {
    val res = delete.where(_.account_id eqs email).and(_.assembly_id eqs id).future()
    Await.result(res, 5.seconds).successNel
  }
}

object EventsContainer extends ConcreteEventsContainer {

  private def mkEventsContainerSack(email: String, input: String): ValidationNel[Throwable, EventsContainerResult] = {
    val nelConts: ValidationNel[Throwable, EventsContainerInput] = (Validation.fromTryCatchThrowable[EventsContainerInput, Throwable] {
      parse(input).extract[EventsContainerInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
    for {
      con <- nelConts
      uir <- (UID("EVC").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      new EventsContainerResult(uir.get._1 + uir.get._2, email, DateHelper.now(), con.assembly_id, con.event_type, con.data, "Megam::EventsContainer")
    }
  }


  def create(email: String, input: String): ValidationNel[Throwable, Option[EventsContainerResult]] = {
    for {
      wa <- (mkEventsContainerSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.GREEN, Console.BOLD, "EventsContainer","|+| ✔", Console.RESET))
      wa.some
    }
  }


  def findByEmail(accountID: String, limit: String): ValidationNel[Throwable, Seq[EventsContainerResult]] = {
    (listRecords(accountID, limit) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsContainerResult] =>
    if (!nm.isEmpty)
      Validation.success[Throwable, Seq[EventsContainerResult]](nm).toValidationNel
     else    Validation.failure[Throwable, Seq[EventsContainerResult]](new ResourceItemNotFound(accountID, "EventsContainer = nothing found.")).toValidationNel
    }
  }

  def IndexEmail(accountID: String): ValidationNel[Throwable, Seq[EventsContainerResult]] = {
    (indexRecords(accountID) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsContainerResult] =>
    if (!nm.isEmpty)
      Validation.success[Throwable, Seq[EventsContainerResult]](nm).toValidationNel
     else  Validation.failure[Throwable, Seq[EventsContainerResult]](new ResourceItemNotFound(accountID, "EventsContainer = nothing found.")).toValidationNel

    }

  }

  def findById(email: String, input: String, limit: String): ValidationNel[Throwable, Seq[EventsContainerResult]] = {
    (mkEventsContainerSack(email, input) leftMap { err: NonEmptyList[Throwable] => err
    }).flatMap { ws: EventsContainerResult =>
      (getRecords(email, ws.created_at.withTimeAtStartOfDay(), ws.assembly_id, limit) leftMap { t: NonEmptyList[Throwable] =>
        new ResourceItemNotFound(ws.assembly_id, "Events = nothing found.")
      }).toValidationNel.flatMap { nm: Seq[EventsContainerResult] =>
        if (!nm.isEmpty)   Validation.success[Throwable, Seq[EventsContainerResult]](nm).toValidationNel
        else  Validation.failure[Throwable, Seq[EventsContainerResult]](new ResourceItemNotFound(ws.assembly_id, "EventsContainer = nothing found.")).toValidationNel
      }
    }
  }

  def delete(email: String): ValidationNel[Throwable, Option[EventsContainerResult]] = {
    deleteRecords(email) match {
      case Success(value) => Validation.success[Throwable, Option[EventsContainerResult]](none).toValidationNel
      case Failure(err) => Validation.success[Throwable, Option[EventsContainerResult]](none).toValidationNel
    }
  }

  def deleteByAssembly(id: String, email: String): ValidationNel[Throwable, Option[EventsContainerResult]] = {
    deleteRecordsByAssembly(id, email) match {
      case Success(value) => Validation.success[Throwable, Option[EventsContainerResult]](none).toValidationNel
      case Failure(err) => Validation.success[Throwable, Option[EventsContainerResult]](none).toValidationNel
    }
  }

}
