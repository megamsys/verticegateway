package models.admin

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import cache._
import db._
import io.megam.auth.funnel.FunnelErrors._

import io.megam.common.uid.UID
import io.megam.util.Time
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

import java.util.UUID
import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.annotation.tailrec

import models.tosca.{ KeyValueField, KeyValueList}

import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.{DateTimeFormat,ISODateTimeFormat}

import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import controllers.stack.ImplicitJsonFormats

import models.Constants._;
import utils.{DateHelper, StringStuff}

case class FlavorInput( name: String, cpu: String, ram: String, disk: String, category: List[String],
      regions: List[String], price: KeyValueList, properties: KeyValueList, status: String)

case class FlavorResult(
  id: String,
  name: String,
  cpu: String,
  ram: String,
  disk: String,
  category: List[String],
  regions: List[String],
  price: KeyValueList,
  properties: KeyValueList,
  status: String,
  json_claz: String,
  updated_at: DateTime,
  created_at: DateTime)

sealed class FlavorSacks extends CassandraTable[FlavorSacks, FlavorResult] with ImplicitJsonFormats  {

  object id extends StringColumn(this) with PartitionKey[String]
  object name extends StringColumn(this) with PrimaryKey[String]

  object cpu extends StringColumn(this)
  object ram extends StringColumn(this)
  object disk extends StringColumn(this)
  object category extends ListColumn[FlavorSacks, FlavorResult, String](this)

  object regions extends ListColumn[FlavorSacks, FlavorResult, String](this)

  object price extends JsonListColumn[FlavorSacks, FlavorResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object properties extends JsonListColumn[FlavorSacks, FlavorResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object status extends StringColumn(this)


  object json_claz extends StringColumn(this)
  object updated_at extends DateTimeColumn(this)
  object created_at extends DateTimeColumn(this)

  override def fromRow(r: Row): FlavorResult = {
    FlavorResult(
      id(r),
      name(r),
      cpu(r),
      ram(r),
      disk(r),
      category(r),
      regions(r),
      price(r),
      properties(r),
      status(r),
      json_claz(r),
      updated_at(r),
      created_at(r))
  }

}


abstract class ConcreteFlavors extends FlavorSacks with  RootConnector {

  override lazy val tableName = "flavors"

  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def getRecordById(id: String): ValidationNel[Throwable, Option[FlavorResult]] = {
    val res = select.allowFiltering().where(_.id eqs id).one()
    Await.result(res, 5.seconds).successNel
  }

  def getRecordByName(name: String): ValidationNel[Throwable, Option[FlavorResult]] = {
    val res = select.where(_.name eqs name).one()
    Await.result(res, 5.seconds).successNel
  }

  def listAllRecords(): ValidationNel[Throwable, Seq[FlavorResult]] = {
    val res = select.consistencyLevel_=(ConsistencyLevel.ONE).fetch

    Await.result(res, 5.seconds).successNel
  }

  def insertNewRecord(mpr: FlavorResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, mpr.id)
      .value(_.name, mpr.name)
      .value(_.cpu, mpr.cpu)
      .value(_.ram, mpr.ram)
      .value(_.disk,mpr.disk)
      .value(_.category,mpr.category)
      .value(_.regions, mpr.regions)
      .value(_.price, mpr.price)
      .value(_.properties, mpr.properties)
      .value(_.status, mpr.status)
      .value(_.json_claz, mpr.json_claz)
      .value(_.updated_at, mpr.updated_at)
      .value(_.created_at, mpr.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def updateRecord(email: String, rip: FlavorResult, aor: Option[FlavorResult]): ValidationNel[Throwable, ResultSet] = {
    val oldstatus  = aor.get.status
    val newstatus  = rip.status

    val res = update.where(_.name eqs rip.name).and(_.id eqs rip.id)
      .modify(_.status setTo StringStuff.NilOrNot(newstatus, oldstatus))
      .and(_.updated_at setTo DateHelper.now())
      .future()
      scala.concurrent.Await.result(res, 5.seconds).successNel
  }

  def deleteRecord(account_id: String, name: String): ValidationNel[Throwable, ResultSet] = {
    val res = delete.where(_.name eqs name).future()
    Await.result(res,5.seconds).successNel
  }
 }


object Flavors extends ConcreteFlavors {

  private def mkFlavorsSack(email: String, input: String): ValidationNel[Throwable, FlavorResult] = {
    val mktsInput: ValidationNel[Throwable, FlavorInput] = (Validation.fromTryCatchThrowable[FlavorInput, Throwable] {
      parse(input).extract[FlavorInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel

    for {
      mkt <- mktsInput
      uir <- (UID("FLV").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      (new FlavorResult(uir.get._1 + uir.get._2, mkt.name, mkt.cpu, mkt.ram, mkt.disk, mkt.category,
        mkt.regions, mkt.price, mkt.properties, mkt.status, models.Constants.FLAVORCLAZ, DateHelper.now(),  DateHelper.now()))
    }
  }

  def listAll: ValidationNel[Throwable, Seq[FlavorResult]] = {
    (listAllRecords() leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound("", "Flavor items = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[FlavorResult] =>
      if (!nm.isEmpty) {
        Validation.success[Throwable, Seq[FlavorResult]](nm.sortWith(_.name < _.name).sortWith(_.cpu < _.cpu)).toValidationNel

      } else {
        Validation.failure[Throwable, Seq[FlavorResult]](new ResourceItemNotFound("", "Flavor = nothing found.")).toValidationNel
      }
    }
  }

  def create(email: String, input: String): ValidationNel[Throwable, Option[FlavorResult]] = {
    for {
      wa <- (mkFlavorsSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.GREEN, Console.BOLD, "Flavors","|+| ✔", Console.RESET))
      wa.some
    }
  }

  def delete(email: String, name: String): ValidationNel[Throwable, FlavorResult] = {
    for {
      wa <- (findByName(name, email) leftMap { t: NonEmptyList[Throwable] => t })
      set <- (deleteRecord(email, name) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.RED, Console.BOLD, "Flavors","|-| ✔", Console.RESET))
      wa
    }
  }

  def update(email: String, input: String): ValidationNel[Throwable, FlavorResult] = {
    val ripNel: ValidationNel[Throwable, FlavorResult] = (Validation.fromTryCatchThrowable[FlavorResult,Throwable] {
      parse(input).extract[FlavorResult]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
    for {
      rip <- ripNel
      wa  <- (findByName(rip.name, email) leftMap { t: NonEmptyList[Throwable] => t })
      set <- updateRecord(email, rip, wa.some)
    } yield {
      wa
    }
  }

 def findById(id: String): ValidationNel[Throwable, Seq[FlavorResult]] = {
    (getRecordById(id) leftMap { t: NonEmptyList[Throwable] ⇒
      new ServiceUnavailableError(id, (t.list.map(m ⇒ m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { xso: Option[FlavorResult] ⇒
      xso match {
        case Some(xs) ⇒ {
          Validation.success[Throwable, Seq[FlavorResult]](List(xs)).toValidationNel
        }
        case None ⇒ Validation.failure[Throwable, Seq[FlavorResult]](new ResourceItemNotFound(id, "")).toValidationNel
      }
    }
  }

  def findByName(name: String, email: String): ValidationNel[Throwable, FlavorResult] = {
    (getRecordByName(name) leftMap { t: NonEmptyList[Throwable] ⇒
      new ServiceUnavailableError(name, (t.list.map(m ⇒ m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { xso: Option[FlavorResult] ⇒
      xso match {
        case Some(xs) ⇒ {
          Validation.success[Throwable, FlavorResult](xs).toValidationNel
        }
        case None ⇒ Validation.failure[Throwable, FlavorResult](new ResourceItemNotFound(name, "")).toValidationNel
      }
    }
  }

}
