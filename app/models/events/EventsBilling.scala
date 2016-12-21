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
import models.base._
import wash._

import utils.DateHelper
import io.megam.util.Time
import org.joda.time.{DateTime, DateTimeZone}
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
import controllers.stack.ImplicitJsonFormats

/**
 * @author rajesh
 *
 */
case class EventsBillingInput(account_id: String,
                              assembly_id: String,
                              event_type: String,
                              data: KeyValueList)

case class EventsBillingResult(id: String,
                              account_id: String,
                              created_at: DateTime,
                              assembly_id: String,
                              event_type: String,
                              data: models.tosca.KeyValueList,
                              json_claz: String)

object EventsBillingResult {
  def apply(id: String,
            account_id: String,
            created_at: DateTime,
            assembly_id: String,
            event_type: String,
            data: models.tosca.KeyValueList) = new EventsBillingResult(id, account_id, created_at, assembly_id, event_type, data, "Megam::EventsBilling")
}

sealed class EventsBillingSacks extends CassandraTable[EventsBillingSacks, EventsBillingResult] with ImplicitJsonFormats {

  object account_id extends StringColumn(this) with PartitionKey[String]
  object created_at extends DateTimeColumn(this) with PrimaryKey[DateTime]
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

  object id extends StringColumn(this)
  object json_claz extends StringColumn(this)

  def fromRow(row: Row): EventsBillingResult = {
    EventsBillingResult(
      id(row),
      account_id(row),
      created_at(row),
      assembly_id(row),
      event_type(row),
      data(row),
      json_claz(row))
  }
}

abstract class ConcreteEventsBilling extends EventsBillingSacks with RootConnector {
  override lazy val tableName = "events_for_billings"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(evt: EventsBillingResult): ValidationNel[Throwable, ResultSet] = {
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

  def listRecords(email: String, limit: String): ValidationNel[Throwable, Seq[EventsBillingResult]] = {
    val count = (if (limit == "0")  "10" else  limit)
    val res = select.where(_.account_id eqs email).limit(count.toInt).fetch()
    Await.result(res, 5.seconds).successNel
    }


  def indexRecords(email: String): ValidationNel[Throwable, Seq[EventsBillingResult]] = {
    val res = select.where(_.account_id eqs email).fetch()
    Await.result(res, 5.seconds).successNel

  }
  def getRecords(created_at: DateTime,assembly_id: String, limit: String): ValidationNel[Throwable, Seq[EventsBillingResult]] = {
    val count = (if (limit == "0")  "10" else  limit)
     val times = DateHelper.toTimeRange(created_at)
     val res = select.allowFiltering().where(_.created_at gte times._1).and(_.created_at lte times._2).and(_.assembly_id eqs assembly_id).limit(count.toInt).fetch()
    Await.result(res, 5.seconds).successNel
     }
}

object EventsBilling extends ConcreteEventsBilling {

private def mkEventsBillingSack(email: String, input: String): ValidationNel[Throwable, EventsBillingResult] = {
  val nelBill: ValidationNel[Throwable, EventsBillingInput] = (Validation.fromTryCatchThrowable[EventsBillingInput, Throwable] {
    parse(input).extract[EventsBillingInput]
  } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
  for {
    bil <- nelBill
    uir <- (UID("EVB").get leftMap { ut: NonEmptyList[Throwable] => ut })
  } yield {
     new EventsBillingResult(uir.get._1 + uir.get._2,email,DateHelper.now(),bil.assembly_id, bil.event_type, bil.data, "Megam::EventsBilling")
  }
}


def create(email: String, input: String): ValidationNel[Throwable, Option[EventsBillingResult]] = {
  for {
    wa <- (mkEventsBillingSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
    set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
  } yield {
    play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "EventsBilling.created success", Console.RESET))
    wa.some
  }
}

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
    (getRecords(ws.created_at.withTimeAtStartOfDay(),ws.assembly_id, limit) leftMap { t: NonEmptyList[Throwable] =>
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
