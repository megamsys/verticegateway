package models.base

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import models.base._
import db._
import cache._
import app.MConfig
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._

import io.megam.common.amqp.response.AMQPResponse
import io.megam.common.amqp._
import io.megam.common.uid._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz.{ Result, UncategorizedError }
import java.nio.charset.Charset

import utils.DateHelper
import io.megam.util.Time
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.{DateTimeFormat,ISODateTimeFormat}

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._
import controllers.stack.ImplicitJsonFormats

/**
 * @author ram
 */
case class RequestInput(account_id: String,
                        cat_id: String,
                        cattype: String,
                        name: String,
                        action: String,
                        category: String) {
  val half_json = "\"account_id\":\"" + account_id + "\",\"cat_id\":\"" + cat_id + "\",\"cattype\":\"" + cattype + "\",\"name\":\"" + name + "\",\"action\":\"" + action + "\",\"category\":\"" + category + "\""
  val json = "{" + half_json + "}"
}

case class RequestResult(id: String, account_id: String, cat_id: String, cattype: String, name: String, action: String, category: String, created_at: DateTime) {

  def toMap: Map[String, String] = {
    Map[String, String](
      ("id" -> id),
      ("account_id" -> account_id),
      ("cat_id" -> cat_id),
      ("cattype" -> cattype),
      ("name" -> name),
      ("action" -> action),
      ("category" -> category))
  }

  def topicFunc(x: Unit): Option[String] = {
    val nsqcontainers = play.api.Play.application(play.api.Play.current).configuration.getString("nsq.topic.containers")
    val nsqvms = play.api.Play.application(play.api.Play.current).configuration.getString("nsq.topic.vms")


    if (cattype.toLowerCase.contains(CATTYPE_DOCKER)) {
      nsqcontainers
    } else if (inMachine(cattype) || inLifecycle(action)) {
      nsqvms
    } else if (name.trim.length > 0) {
      name.some
    } else none
  }

  //if its create, delete and cattype is a machine, drop it in nsqvms
  private def  inMachine(cat: String) =  {
    val DQACTIONS = Array[String](RESETPW)

    (cat.toLowerCase.contains(CATTYPE_TORPEDO) && !DQACTIONS.contains(action))
  }

  private def inLifecycle(action: String) = {
    val DQACTIONS = Array[String](CREATE, DELETE)
    DQACTIONS.contains(action)
  }
}

object RequestResult {
  def apply = new RequestResult(new String(), new String(), new String(), new String(), new String(), new String(), new String(), DateHelper.now())
}

sealed class RequestSacks extends CassandraTable[RequestSacks, RequestResult] with ImplicitJsonFormats {

  object id extends StringColumn(this) with PrimaryKey[String]
  object account_id extends StringColumn(this)
  object cat_id extends StringColumn(this)
  object cattype extends StringColumn(this)
  object name extends StringColumn(this)
  object action extends StringColumn(this)
  object category extends StringColumn(this)
  object created_at extends DateTimeColumn(this)

  def fromRow(row: Row): RequestResult = {
    RequestResult(
      id(row),
      account_id(row),
      cat_id(row),
      cattype(row),
      name(row),
      action(row),
      category(row),
      created_at(row))
  }
}

abstract class ConcreteRequests extends RequestSacks with RootConnector {

  override lazy val tableName = "requests"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(ams: RequestResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, ams.id)
      .value(_.account_id, ams.account_id)
      .value(_.cat_id, ams.cat_id)
      .value(_.cattype, ams.cattype)
      .value(_.name, ams.name)
      .value(_.action, ams.action)
      .value(_.category, ams.category)
      .value(_.created_at, ams.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def getRecord(id: String): ValidationNel[Throwable, Option[RequestResult]] = {
    val res = select.allowFiltering().where(_.id eqs id).one()
    Await.result(res, 5.seconds).successNel
  }

}

object Requests extends ConcreteRequests {

  private def mkRequestSack(input: String): ValidationNel[Throwable, Option[RequestResult]] = {
    val ripNel: ValidationNel[Throwable, RequestInput] = (Validation.fromTryCatchThrowable[models.base.RequestInput, Throwable] {
      parse(input).extract[RequestInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel

    for {
      rip <- ripNel
      uir <- (UID("rip").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val res = RequestResult((uir.get._1 + uir.get._2), rip.account_id, rip.cat_id, rip.cattype, rip.name, rip.action, rip.category, DateHelper.now())
      res.some
    }
  }

  def create(input: String): ValidationNel[Throwable, Option[wash.PQd]] = {
    for {
      ogsi <- mkRequestSack(input) leftMap { err: NonEmptyList[Throwable] => err }
      ogsr <- (insertNewRecord(ogsi.get) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.GREEN, Console.BOLD, "Request","|+| ✔",Console.RESET))
      new wash.PQd(ogsi.get.topicFunc, MessagePayLoad(Messages(ogsi.get.toMap.toList)).toJson(false)).some
    }
  }


  def findById(email: String, id: String): ValidationNel[Throwable, Option[RequestResult]] = {
    (getRecord(id) leftMap { t: NonEmptyList[Throwable] ⇒
      new ServiceUnavailableError(id, (t.list.map(m ⇒ m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { xso: Option[RequestResult] ⇒
      xso match {
        case Some(xs) ⇒ {
          Validation.success[Throwable, Option[RequestResult]](xs.some).toValidationNel
        }
        case None ⇒ Validation.failure[Throwable, Option[RequestResult]](new ResourceItemNotFound(id, "")).toValidationNel
      }
    }
  }

  // create a request and publish
  def createAndPub(email: String, input: String): ValidationNel[Throwable, Option[wash.PQd]] = {
    (create(input) leftMap { err: NonEmptyList[Throwable] =>
      err
    }).flatMap { pq: Option[wash.PQd] =>
      if (!MConfig.mute_emails.contains(email)) {
        new wash.AOneWasher(pq.get).wash flatMap { maybeGS: AMQPResponse =>
          play.api.Logger.debug(("%s%s%-20s%s%s").format(Console.GREEN, Console.BOLD, "Request.pub","|+| "+input+" ✔", Console.RESET))
          pq.successNel[Throwable]
        }
      } else {
        play.api.Logger.debug(("%s%s%-20s%s%s").format(Console.RED, Console.BOLD, "Request.pub","|+| ✗", Console.RESET))
        wash.PQd.empty.some.successNel[Throwable]
      }
    }
  }

}
