package models.disks

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import cache._
import db._
import models.base.RequestInput
import models.tosca.{ KeyValueField, KeyValueList}
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._

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

import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import controllers.stack.ImplicitJsonFormats


/**
 * @author ranjitha
 *
 */
case class RawImagesInput( org_id: String, account_id: String, name: String, status: String, repos: String, inputs: models.tosca.KeyValueList)

case class RawImagesResult(
  id: String,
  org_id: String,
  account_id: String,
  name:   String,
  status: String,
  repos: String,
  inputs: models.tosca.KeyValueList,
  outputs: models.tosca.KeyValueList,
  json_claz: String,
  created_at: DateTime,
  updated_at: DateTime
  )

object RawImagesResult {
  def apply(id: String, org_id: String, account_id: String, name: String, status: String, repos: String, inputs: models.tosca.KeyValueList, outputs: models.tosca.KeyValueList) = new RawImagesResult(id, org_id, account_id, name, status, repos, inputs, outputs, models.Constants.RAWIMAGESCLAZ, DateTime.now(),DateTime.now())
}

sealed class RawImagesSacks extends CassandraTable[RawImagesSacks, RawImagesResult] with ImplicitJsonFormats {

  object id extends StringColumn(this) with  PartitionKey[String]
  object org_id extends StringColumn(this)
  object account_id extends StringColumn(this) with PrimaryKey[String]
  object name extends StringColumn(this)
  object status extends StringColumn(this)
  object repos extends StringColumn(this)


  object inputs extends JsonListColumn[RawImagesSacks, RawImagesResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object outputs extends JsonListColumn[RawImagesSacks, RawImagesResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object created_at extends DateTimeColumn(this) with PrimaryKey[DateTime]
  object updated_at extends DateTimeColumn(this)
  object json_claz extends StringColumn(this)

  def fromRow(row: Row): RawImagesResult = {
    RawImagesResult(
      id(row),
      org_id(row),
      account_id(row),
      name(row),
      status(row),
      repos(row),
      inputs(row),
      outputs(row),
      json_claz(row),
      created_at(row),
      updated_at(row))
  }
}

abstract class ConcreteRawImages extends RawImagesSacks with RootConnector {

  override lazy val tableName = "rawimages"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(sps: RawImagesResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, sps.id)
      .value(_.org_id, sps.org_id)
      .value(_.account_id, sps.account_id)
      .value(_.name, sps.name)
      .value(_.status, sps.status)
      .value(_.repos, sps.repos)
      .value(_.inputs, sps.inputs)
      .value(_.outputs, sps.outputs)
      .value(_.json_claz, sps.json_claz)
      .value(_.created_at, sps.created_at)
      .value(_.updated_at, sps.updated_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def updateRecord(email: String, rip: RawImagesResult, aor: Option[RawImagesResult]): ValidationNel[Throwable, ResultSet] = {
    val oldstatus  = aor.get.status
    val newstatus  = rip.status

    val res = update.where(_.id eqs rip.id)
        .and(_.account_id eqs email)
      .modify(_.status setTo StringStuff.NilOrNot(newstatus, oldstatus))
      .and(_.repos setTo rip.repos)
      .and(_.inputs setTo rip.inputs)
      .and(_.outputs setTo rip.outputs)
      .and(_.updated_at setTo DateHelper.now())
      .future()
      Await.result(res, 5.seconds).successNel
  }

  def listRecords(email: String): ValidationNel[Throwable, Seq[RawImagesResult]] = {
    val res = select.where(_.account_id eqs email).fetch()
    Await.result(res, 5.seconds).successNel
  }

  def getRecord(id: String): ValidationNel[Throwable, Option[RawImagesResult]] = {
    val res = select.allowFiltering().where(_.id eqs id).one()
    Await.result(res, 5.seconds).successNel
  }

    def listAllRecords: ValidationNel[Throwable, Seq[RawImagesResult]] = {
    val res = select.fetch
    Await.result(res, 5.seconds).successNel
  }

  def deleteRecords(email: String): ValidationNel[Throwable, ResultSet] = {
    val res = delete.where(_.account_id eqs email).future()
    Await.result(res, 5.seconds).successNel
  }
}

object RawImages extends ConcreteRawImages {

private def mkRawImagesSack(email: String, input: String): ValidationNel[Throwable, RawImagesResult] = {
  val rawimagesInput: ValidationNel[Throwable, RawImagesInput] = (Validation.fromTryCatchThrowable[RawImagesInput, Throwable] {
    parse(input).extract[RawImagesInput]
  } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel

  play.api.Logger.info(("%s%s%-20s%s").format(Console.MAGENTA, Console.BOLD, rawimagesInput ,Console.RESET))

  for {
    raw <- rawimagesInput
    uir <- (UID("raw").get leftMap { ut: NonEmptyList[Throwable] => ut })
  } yield {
    (new RawImagesResult(uir.get._1 + uir.get._2, raw.org_id, email, raw.name, raw.status, raw.repos, raw.inputs, List(), models.Constants.RAWIMAGESCLAZ, DateHelper.now(), DateHelper.now()))
  }
}

//Admin authority can list all snapshots for 1.5.
def list: ValidationNel[Throwable, Seq[RawImagesResult]] = {
  listAllRecords match {
    case Success(value) => Validation.success[Throwable, Seq[RawImagesResult]](value).toValidationNel
    case Failure(err) => Validation.success[Throwable, Seq[RawImagesResult]](List()).toValidationNel
  }

}

def create(email: String, input: String): ValidationNel[Throwable, Option[RawImagesResult]] = {
  for {
    wa <- (mkRawImagesSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
    set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
  } yield {
    play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.GREEN, Console.BOLD, "RawImages","|+| ✔", Console.RESET))
    atPub(email, wa)
    wa.some
  }
}

def delete(email: String, id: String): ValidationNel[Throwable, RawImagesResult] = {
  for {
    wa <- (findById(id) leftMap { t: NonEmptyList[Throwable] => t })
    se <- (deleteRecords(email) leftMap { t: NonEmptyList[Throwable] => t })
  } yield {
    play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.RED, Console.BOLD, "RawImages","|-| ✔", Console.RESET))
    wa.head
  }
}

def update(email: String, input: String): ValidationNel[Throwable, RawImagesResult] = {
  val ripNel: ValidationNel[Throwable, RawImagesResult] = (Validation.fromTryCatchThrowable[RawImagesResult,Throwable] {
    parse(input).extract[RawImagesResult]
  } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
  for {
    rip <- ripNel
    qor <- (findById(rip.id) leftMap { t: NonEmptyList[Throwable] => t })
    set <- updateRecord(email, rip, qor.head.some)
  } yield {
    qor.head
  }
}

  def findByEmail(accountId: String): ValidationNel[Throwable, Seq[RawImagesResult]] = {
    (listRecords(accountId) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountId, "RawImages = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[RawImagesResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[RawImagesResult]](nm).toValidationNel
      else
        Validation.success[Throwable, Seq[RawImagesResult]](List[RawImagesResult]()).toValidationNel
    }
  }

  def deleteByEmail(email: String): ValidationNel[Throwable, RawImagesResult] = {
    for {
      sa <- (findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      df <- deleteFound(email, sa)
    } yield df
  }

  def findById(id: String): ValidationNel[Throwable, Seq[RawImagesResult]] = {
    (getRecord(id) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(id, "RawImages = nothing found.")
    }).toValidationNel.flatMap { xso: Option[RawImagesResult] ⇒
      xso match {
        case Some(xs) ⇒ {
          Validation.success[Throwable, Seq[RawImagesResult]](List(xs)).toValidationNel
        }
        case None ⇒ Validation.failure[Throwable, Seq[RawImagesResult]](new ResourceItemNotFound(id, "")).toValidationNel
      }
    }
  }

  private def deleteFound(email: String, ri: Seq[RawImagesResult]) = {
      val output = (ri.map { sas =>
        play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.RED, Console.BOLD, "RawImages","|-| ✔", Console.RESET))
        dePub(email, sas)
      })

      if (!output.isEmpty)
         output.head
      else
        RawImagesResult("","","","","","", models.tosca.KeyValueList.empty, models.tosca.KeyValueList.empty).successNel

  }


  private def atPub(email: String, wa: RawImagesResult): ValidationNel[Throwable, RawImagesResult] = {
    models.base.Requests.createAndPub(email, RequestInput(email, wa.id, CATTYPE_MARKETPLACES, "", CREATE_RAWIMAGE, RAWIMAGES).json)
    wa.successNel[Throwable]
  }

  private def dePub(email: String, wa: RawImagesResult): ValidationNel[Throwable, RawImagesResult] = {
    models.base.Requests.createAndPub(email, RequestInput(email, wa.id, CATTYPE_MARKETPLACES, "", DELETE_RAWIMAGE, RAWIMAGES).json)
    wa.successNel[Throwable]
  }

}
