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
 * @author ranjitha
 *
 */
case class EventsSkewsInput(account_id: String,
                              cat_id: String,
                              event_type: String,
                              inputs: models.tosca.KeyValueList,
                              actions: models.tosca.KeyValueList,
                              status: String )

case class EventsSkewsResult(id: String,
                              account_id: String,
                              cat_id: String,
                              event_type: String,
                              status: String,
                              inputs: models.tosca.KeyValueList,
                              outputs: models.tosca.KeyValueList,
                              actions: models.tosca.KeyValueList,
                              json_claz: String,
                              created_at: DateTime,
                              updated_at: DateTime
                              )



sealed class EventsSkewsSacks extends CassandraTable[EventsSkewsSacks, EventsSkewsResult] with ImplicitJsonFormats {
  object id extends StringColumn(this)
  object account_id extends StringColumn(this) with PartitionKey[String]
  object cat_id extends StringColumn(this) with PrimaryKey[String]
  object created_at extends DateTimeColumn(this)
  object updated_at extends DateTimeColumn(this)
  object status extends StringColumn(this)
  object event_type extends StringColumn(this)

  object inputs extends JsonListColumn[EventsSkewsSacks, EventsSkewsResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }
  object outputs extends JsonListColumn[EventsSkewsSacks, EventsSkewsResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object actions extends JsonListColumn[EventsSkewsSacks, EventsSkewsResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object json_claz extends StringColumn(this)

  def fromRow(row: Row): EventsSkewsResult = {
    EventsSkewsResult(
      id(row),
      account_id(row),
      cat_id(row),
      event_type(row),
      status(row),
      inputs(row),
      outputs(row),
      actions(row),
      json_claz(row),
      created_at(row),
      updated_at(row)
      )
  }
}

abstract class ConcreteEventsSkews extends EventsSkewsSacks with RootConnector {
  override lazy val tableName = "events_skews"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(evt: EventsSkewsResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, evt.id)
      .value(_.account_id, evt.account_id)
      .value(_.cat_id, evt.cat_id)
      .value(_.event_type, evt.event_type)
      .value(_.status, evt.status)
      .value(_.inputs, evt.inputs)
      .value(_.outputs, evt.outputs)
      .value(_.actions, evt.actions)
      .value(_.created_at, evt.created_at)
      .value(_.updated_at, evt.updated_at)
      .value(_.json_claz, evt.json_claz)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(email: String): ValidationNel[Throwable, Seq[EventsSkewsResult]] = {
    val res = select.where(_.account_id eqs email).fetch()
    Await.result(res, 5.seconds).successNel
    }

  def getRecords(cat_id: String, email: String): ValidationNel[Throwable, Seq[EventsSkewsResult]] = {
   val res = select.allowFiltering().where(_.account_id eqs email).and(_.cat_id eqs cat_id).fetch()
    Await.result(res, 5.seconds).successNel
  }

}

object EventsSkews extends ConcreteEventsSkews {

private def mkEventsSkewsSack(email: String, input: String): ValidationNel[Throwable, EventsSkewsResult] = {
  val nelBill: ValidationNel[Throwable, EventsSkewsInput] = (Validation.fromTryCatchThrowable[EventsSkewsInput, Throwable] {
    parse(input).extract[EventsSkewsInput]
  } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
  for {
    bil <- nelBill
    uir <- (UID("EVSK").get leftMap { ut: NonEmptyList[Throwable] => ut })
  } yield {
     new EventsSkewsResult(uir.get._1 + uir.get._2,email,bil.cat_id, bil.event_type, bil.status, bil.inputs, List(), bil.actions,   "Megam::EventsBilling", DateHelper.now(), DateHelper.now())
  }
}


def create(email: String, input: String): ValidationNel[Throwable, Option[EventsSkewsResult]] = {
  for {
    wa <- (mkEventsSkewsSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
    set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
  } yield {
    play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.GREEN, Console.BOLD, "EventsSkews","|+| ✔", Console.RESET))
    wa.some
  }
}

  def findByEmail(accountID: String): ValidationNel[Throwable, Seq[EventsSkewsResult]] = {
    (listRecords(accountID) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsSkewsResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[EventsSkewsResult]](nm).toValidationNel
       else
        Validation.failure[Throwable, Seq[EventsSkewsResult]](new ResourceItemNotFound(accountID, "EventsSkews = nothing found.")).toValidationNel

    }
  }

  def findById(cat_id: String, email: String): ValidationNel[Throwable, Seq[EventsSkewsResult]] = {
    (getRecords(cat_id,email) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(cat_id, "EventsSkews = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[EventsSkewsResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[EventsSkewsResult]](nm).toValidationNel
      else
      Validation.success[Throwable, Seq[EventsSkewsResult]](List[EventsSkewsResult]()).toValidationNel
    }

  }

}
