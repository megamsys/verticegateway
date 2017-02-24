package models.base

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import cache._
import db._
import models.Constants._
import models.json.tosca._
import models.json.tosca.carton._
import io.megam.auth.funnel.FunnelErrors._
import models.tosca.{ KeyValueField, KeyValueList}

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._


import utils.{DateHelper, StringStuff}
import io.megam.util.Time
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.{DateTimeFormat,ISODateTimeFormat}
import utils.DateHelper

import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import controllers.stack.ImplicitJsonFormats

case class MarketPlaceInput( flavor: String, provided_by: String, cattype: String, catorder: String,
  status: String, image: String, url: String, envs: KeyValueList,
  options: KeyValueList, inputs: KeyValueList, acl_policies: KeyValueList,  plans: KeyValueList)

case class MarketPlaceResult(
  id: String,
  flavor: String,
  provided_by: String,
  cattype: String,
  catorder: String,
  status: String,
  image: String,
  url: String,
  envs: KeyValueList,
  options: KeyValueList,
  inputs: KeyValueList,
  outputs: KeyValueList,
  acl_policies: KeyValueList,
  plans: KeyValueList,
  json_claz: String,
  updated_at: DateTime,
  created_at: DateTime)

sealed class MarketPlaceSacks extends CassandraTable[MarketPlaceSacks, MarketPlaceResult] with ImplicitJsonFormats  {

  object id extends StringColumn(this) with PartitionKey[String]
  object flavor extends StringColumn(this) with PrimaryKey[String]

  object provided_by extends StringColumn(this) with PartitionKey[String]
  object cattype extends StringColumn(this)
  object catorder extends StringColumn(this)
  object status extends StringColumn(this)

  object image extends StringColumn(this)
  object url extends StringColumn(this)

  object envs extends JsonListColumn[MarketPlaceSacks, MarketPlaceResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }
  object options extends JsonListColumn[MarketPlaceSacks, MarketPlaceResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object inputs extends JsonListColumn[MarketPlaceSacks, MarketPlaceResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object outputs extends JsonListColumn[MarketPlaceSacks, MarketPlaceResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object acl_policies extends JsonListColumn[MarketPlaceSacks, MarketPlaceResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object plans extends JsonListColumn[MarketPlaceSacks, MarketPlaceResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object json_claz extends StringColumn(this)
  object updated_at extends DateTimeColumn(this)
  object created_at extends DateTimeColumn(this)

  override def fromRow(r: Row): MarketPlaceResult = {
    MarketPlaceResult(
      id(r),
      flavor(r),
      provided_by(r),
      cattype(r),
      catorder(r),
      status(r),
      image(r),
      url(r),
      envs(r),
      options(r),
      inputs(r),
      outputs(r),
      acl_policies(r),
      plans(r),
      json_claz(r),
      updated_at(r),
      created_at(r))
  }

}


abstract class ConcreteMarketPlaces extends MarketPlaceSacks with  RootConnector {

  override lazy val tableName = "marketplaces"

  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def getRecordById(id: String): ValidationNel[Throwable, Option[MarketPlaceResult]] = {
    val res = select.allowFiltering().where(_.id eqs id).one()
    Await.result(res, 5.seconds).successNel
  }

  def getRecordByFlavor(flavor: String): ValidationNel[Throwable, Option[MarketPlaceResult]] = {
    val res = select.where(_.flavor eqs flavor).one()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(provided_by: String): ValidationNel[Throwable, Seq[MarketPlaceResult]] = {
    val res = select.allowFiltering().where(_.provided_by eqs provided_by).fetch()
    Await.result(res, 5.seconds).successNel
  }

  def listAllRecords: ValidationNel[Throwable, Seq[MarketPlaceResult]] = {
    val res = select.fetch()
    Await.result(res, 5.seconds).successNel
  }

  def insertNewRecord(mpr: MarketPlaceResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, mpr.id)
      .value(_.flavor, mpr.flavor)
      .value(_.cattype, mpr.cattype)
      .value(_.catorder,mpr.catorder)
      .value(_.status, mpr.status)
      .value(_.image, mpr.image)
      .value(_.url, mpr.url)
      .value(_.envs, mpr.envs)
      .value(_.options, mpr.options)
      .value(_.inputs, mpr.inputs)
      .value(_.outputs, mpr.outputs)
      .value(_.acl_policies, mpr.acl_policies)
      .value(_.plans, mpr.plans)
      .value(_.json_claz, mpr.json_claz)
      .value(_.updated_at, mpr.updated_at)
      .value(_.created_at, mpr.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def updateRecord(email: String, rip: MarketPlaceResult, aor: Option[MarketPlaceResult]): ValidationNel[Throwable, ResultSet] = {
    val oldstatus  = aor.get.status
    val newstatus  = rip.status

    val res = update.where(_.flavor eqs rip.flavor).and(_.provided_by eqs rip.provided_by).and(_.id eqs rip.id)
      .modify(_.status setTo StringStuff.NilOrNot(newstatus, oldstatus))
      .and(_.outputs setTo rip.outputs)
      .and(_.updated_at setTo DateHelper.now())
      .future()
      Await.result(res, 5.seconds).successNel
  }

  def deleteRecord(account_id: String, flavor: String): ValidationNel[Throwable, ResultSet] = {
    val res = delete.where(_.flavor eqs flavor).future()
    Await.result(res,5.seconds).successNel
  }
 }


object MarketPlaces extends ConcreteMarketPlaces {

  private def mkMarketPlacesSack(email: String, input: String): ValidationNel[Throwable, MarketPlaceResult] = {
    val mktsInput: ValidationNel[Throwable, MarketPlaceInput] = (Validation.fromTryCatchThrowable[MarketPlaceInput, Throwable] {
      parse(input).extract[MarketPlaceInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel

    for {
      mkt <- mktsInput
      uir <- (UID("MKT").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      (new MarketPlaceResult(uir.get._1 + uir.get._2, mkt.flavor, mkt.provided_by, mkt.cattype, mkt.catorder,
        mkt.status, mkt.image, mkt.url, mkt.envs, mkt.options, mkt.inputs, List(), mkt.acl_policies, mkt.plans, models.Constants.MARKETPLACECLAZ, DateHelper.now(),  DateHelper.now()))
    }
  }

  def listAll: ValidationNel[Throwable, Seq[MarketPlaceResult]] = {
    (listAllRecords leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound("", "Marketplace items = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[MarketPlaceResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[MarketPlaceResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[MarketPlaceResult]](new ResourceItemNotFound("", "Marketplace = nothing found.")).toValidationNel
    }
  }

  def create(email: String, input: String): ValidationNel[Throwable, Option[MarketPlaceResult]] = {
    for {
      wa <- (mkMarketPlacesSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.GREEN, Console.BOLD, "Marketplaces","|+| ✔", Console.RESET))
      atPub(email, wa)
      wa.some
    }
  }

  def delete(email: String, flavor: String): ValidationNel[Throwable, MarketPlaceResult] = {
    for {
      wa <- (findByFlavor(flavor, email) leftMap { t: NonEmptyList[Throwable] => t })
      set <- (deleteRecord(email, flavor) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.RED, Console.BOLD, "MarketPlaces","|-| ✔", Console.RESET))
      dePub(email, wa)
      wa
    }
  }

  def update(email: String, input: String): ValidationNel[Throwable, MarketPlaceResult] = {
    val ripNel: ValidationNel[Throwable, MarketPlaceResult] = (Validation.fromTryCatchThrowable[MarketPlaceResult,Throwable] {
      parse(input).extract[MarketPlaceResult]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
    for {
      rip <- ripNel
      wa  <- (findByFlavor(rip.flavor, email) leftMap { t: NonEmptyList[Throwable] => t })
      set <- updateRecord(email, rip, wa.some)
    } yield {
      upPub(email, wa)
      wa
    }
  }

    def findByProvider(providedBy: String): ValidationNel[Throwable, Seq[MarketPlaceResult]] = {
      (listRecords(providedBy) leftMap { t: NonEmptyList[Throwable] =>
        new ResourceItemNotFound(providedBy, "MarketPlaces = nothing found.")
      }).toValidationNel.flatMap { nm: Seq[MarketPlaceResult] =>
        if (!nm.isEmpty)
          Validation.success[Throwable, Seq[MarketPlaceResult]](nm).toValidationNel
        else
          Validation.success[Throwable, Seq[MarketPlaceResult]](List()).toValidationNel
      }
  }

  def findById(id: String, email: String): ValidationNel[Throwable, MarketPlaceResult] = {
    (getRecordById(id) leftMap { t: NonEmptyList[Throwable] ⇒
      new ServiceUnavailableError(id, (t.list.map(m ⇒ m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { xso: Option[MarketPlaceResult] ⇒
      xso match {
        case Some(xs) ⇒ {
          Validation.success[Throwable, MarketPlaceResult](xs).toValidationNel
        }
        case None ⇒ Validation.failure[Throwable, MarketPlaceResult](new ResourceItemNotFound(id, "")).toValidationNel
      }
    }
  }

  def findByFlavor(mkpFlavor: String, email: String): ValidationNel[Throwable, MarketPlaceResult] = {
    (getRecordByFlavor(mkpFlavor) leftMap { t: NonEmptyList[Throwable] ⇒
      new ServiceUnavailableError(mkpFlavor, (t.list.map(m ⇒ m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { xso: Option[MarketPlaceResult] ⇒
      xso match {
        case Some(xs) ⇒ {
          Validation.success[Throwable, MarketPlaceResult](xs).toValidationNel
        }
        case None ⇒ Validation.failure[Throwable, MarketPlaceResult](new ResourceItemNotFound(mkpFlavor, "")).toValidationNel
      }
    }
  }

  private def atPub(email: String, wa: MarketPlaceResult): ValidationNel[Throwable, MarketPlaceResult] = {
    models.base.Requests.createAndPub(email, RequestInput(email, wa.flavor, CATTYPE_MARKETPLACES, "", INITIALIZE_MARKETPLACE, LOCALSITE_MARKETPLACES).json)
    wa.successNel[Throwable]
  }

  private def dePub(email: String, wa: MarketPlaceResult): ValidationNel[Throwable, MarketPlaceResult] = {
    models.base.Requests.createAndPub(email, RequestInput(email, wa.flavor, CATTYPE_MARKETPLACES, "", CREATE_MARKETPLACE, LOCALSITE_MARKETPLACES).json)
    wa.successNel[Throwable]
  }

  private def upPub(email: String, wa: MarketPlaceResult): ValidationNel[Throwable, MarketPlaceResult] = {
    models.base.Requests.createAndPub(email, RequestInput(email, wa.flavor, CATTYPE_MARKETPLACES, "", DELETE_MARKETPLACE, LOCALSITE_MARKETPLACES).json)
    wa.successNel[Throwable]
  }


}
