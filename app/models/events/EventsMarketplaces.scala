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
import scala.concurrent.{ Future ⇒ ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._
import com.websudos.phantom.iteratee.Iteratee
import controllers.stack.ImplicitJsonFormats


/**
 * @author ranjitha
 *
 */
case class EventsMarketplaceInput(
  account_id: String,
  marketplace_id: String,
  event_type: String,
  data: KeyValueList)

case class EventsMarketplaceResult(
  id: String,
  account_id: String,
  created_at: DateTime,
  marketplace_id: String,
  event_type: String,
  data: models.tosca.KeyValueList,
  json_claz: String)

object EventsMarketplaceResult {
  def apply(id: String, account_id: String, created_at: DateTime, marketplace_id: String, event_type: String, data: models.tosca.KeyValueList) = new EventsMarketplaceResult(id, account_id, created_at, marketplace_id, event_type, data, "Megam::EventsMarketplaces")
}

sealed class EventsMarketplaceSacks extends CassandraTable[EventsMarketplaceSacks, EventsMarketplaceResult] with ImplicitJsonFormats {

  object account_id extends StringColumn(this) with PartitionKey[String]
  object created_at extends DateTimeColumn(this) with PrimaryKey[DateTime]
  object marketplace_id extends StringColumn(this) with PrimaryKey[String]
  object event_type extends StringColumn(this) with PrimaryKey[String]

  object data extends JsonListColumn[EventsMarketplaceSacks, EventsMarketplaceResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }
  object id extends StringColumn(this)
  object json_claz extends StringColumn(this)

  def fromRow(row: Row): EventsMarketplaceResult = {
    EventsMarketplaceResult(
      id(row),
      account_id(row),
      created_at(row),
      marketplace_id(row),
      event_type(row),
      data(row),
      json_claz(row))
  }
}

abstract class ConcreteEventsMarketplaces extends EventsMarketplaceSacks with RootConnector {
  override lazy val tableName = "events_for_marketplaces"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(evt: EventsMarketplaceResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, evt.id)
      .value(_.account_id, evt.account_id)
      .value(_.created_at, evt.created_at)
      .value(_.marketplace_id, evt.marketplace_id)
      .value(_.event_type, evt.event_type)
      .value(_.data, evt.data)
      .value(_.json_claz, evt.json_claz)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(email: String, limit: String): ValidationNel[Throwable, Seq[EventsMarketplaceResult]] = {
    val count = (if (limit == "0")  "10" else  limit)
    val res = select.where(_.account_id eqs email).limit(count.toInt).fetch()
    Await.result(res, 5.seconds).successNel
  }

  def indexRecords(email: String): ValidationNel[Throwable, Seq[EventsMarketplaceResult]] = {
    val res = select.where(_.account_id eqs email).fetch()
    Await.result(res, 5.seconds).successNel

  }

  def getRecords(email: String, created_at: DateTime, marketplace_id: String, limit: String): ValidationNel[Throwable, Seq[EventsMarketplaceResult]] = {
    val count = (if (limit == "0")  "10" else  limit)
     val times = DateHelper.toTimeRange(created_at)
    val res = select.allowFiltering().where(_.created_at gte times._1).and(_.created_at lte times._2).and(_.marketplace_id eqs marketplace_id).limit(count.toInt).fetch()
     Await.result(res, 5.seconds).successNel
  }

  def deleteRecords(email: String): ValidationNel[Throwable, ResultSet] = {
    val res = delete.where(_.account_id eqs email).future()
    Await.result(res, 5.seconds).successNel
  }

  def deleteRecordsByMarketplace(id: String, email: String): ValidationNel[Throwable, ResultSet] = {
    val res = delete.where(_.account_id eqs email).and(_.marketplace_id eqs id).future()
    Await.result(res, 5.seconds).successNel
  }
}

object EventsMarketplaces extends ConcreteEventsMarketplaces {


  private def mkEventsMarketplacesSack(email: String, input: String): ValidationNel[Throwable, EventsMarketplaceResult] = {
    val nelVM: ValidationNel[Throwable, EventsMarketplaceInput] = (Validation.fromTryCatchThrowable[EventsMarketplaceInput, Throwable] {
      parse(input).extract[EventsMarketplaceInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      vm <-  nelVM
      uir <- (UID("EVM").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {

      val bvalue = Set(email)
      val json = new EventsMarketplaceResult(uir.get._1 + uir.get._2, email, DateHelper.now(), vm.marketplace_id, vm.event_type, vm.data, "Megam::EventsMarketplaces")
      json
    }
  }


  def create(email: String, input: String): ValidationNel[Throwable, Option[EventsMarketplaceResult]] = {
    for {
      wa <- (mkEventsMarketplacesSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.GREEN, Console.BOLD, "EventsMarketplaces","|+| ✔", Console.RESET))
      wa.some
    }
  }

 def findByEmail(accountID: String, limit: String): ValidationNel[Throwable, Seq[EventsMarketplaceResult]] = {
    (listRecords(accountID, limit) leftMap { t: NonEmptyList[Throwable] ⇒
      new ResourceItemNotFound(accountID, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsMarketplaceResult] ⇒
      if (!nm.isEmpty) Validation.success[Throwable, Seq[EventsMarketplaceResult]](nm).toValidationNel
      else  Validation.failure[Throwable, Seq[EventsMarketplaceResult]](new ResourceItemNotFound(accountID, "EventsMarketplaces = nothing found.")).toValidationNel
      }
  }

  def IndexEmail(accountID: String): ValidationNel[Throwable, Seq[EventsMarketplaceResult]] = {
    (indexRecords(accountID) leftMap { t: NonEmptyList[Throwable] ⇒
      new ResourceItemNotFound(accountID, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsMarketplaceResult] ⇒
      if (!nm.isEmpty)  Validation.success[Throwable, Seq[EventsMarketplaceResult]](nm).toValidationNel
      else  Validation.failure[Throwable, Seq[EventsMarketplaceResult]](new ResourceItemNotFound(accountID, "EventsMarketplaces = nothing found.")).toValidationNel
    }

  }

  def findById(email: String, input: String, limit: String): ValidationNel[Throwable, Seq[EventsMarketplaceResult]] = {
    (mkEventsMarketplacesSack(email, input) leftMap { err: NonEmptyList[Throwable] ⇒ err
    }).flatMap { ws: EventsMarketplaceResult ⇒
      (getRecords(email, ws.created_at.withTimeAtStartOfDay(), ws.marketplace_id, limit) leftMap { t: NonEmptyList[Throwable] ⇒
        new ResourceItemNotFound(ws.marketplace_id, "Events = nothing found.")
      }).toValidationNel.flatMap { nm: Seq[EventsMarketplaceResult] ⇒
        if (!nm.isEmpty)
          Validation.success[Throwable, Seq[EventsMarketplaceResult]](nm).toValidationNel
        else
          Validation.failure[Throwable, Seq[EventsMarketplaceResult]](new ResourceItemNotFound(ws.marketplace_id, "EventsMarketplaces = nothing found.")).toValidationNel
        }
    }
  }

  def delete(email: String): ValidationNel[Throwable, Option[EventsMarketplaceResult]] = {
    deleteRecords(email) match {
      case Success(value) => Validation.success[Throwable, Option[EventsMarketplaceResult]](none).toValidationNel
      case Failure(err) => Validation.success[Throwable, Option[EventsMarketplaceResult]](none).toValidationNel
    }
  }

  def deleteByAssembly(id: String, email: String): ValidationNel[Throwable, Option[EventsMarketplaceResult]] = {
    deleteRecordsByMarketplace(id, email) match {
      case Success(value) => Validation.success[Throwable, Option[EventsMarketplaceResult]](none).toValidationNel
      case Failure(err) => Validation.success[Throwable, Option[EventsMarketplaceResult]](none).toValidationNel
    }
  }
}
