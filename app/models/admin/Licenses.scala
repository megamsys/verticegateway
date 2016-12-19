package models.admin

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import cache._
import db._
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._

import utils.DateHelper
import io.megam.util.Time
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.{DateTimeFormat,ISODateTimeFormat}

import io.megam.common.uid.UID
import net.liftweb.json._
import controllers.stack.ImplicitJsonFormats
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author rajthilak
 */
case class LicensesInput(data: String) {
  val half_json = "\"data\":\"" + data + "\""

  val json = "{" + half_json + "}"
}


case class LicensesResult(id: String, data: String, created_at: DateTime) {}


sealed class LicensesSack extends CassandraTable[LicensesSack, LicensesResult] with ImplicitJsonFormats {

  object id extends StringColumn(this) with PrimaryKey[String]
  object data extends StringColumn(this)
  object created_at extends DateTimeColumn(this)

  def fromRow(row: Row): LicensesResult = {
    LicensesResult(
      id(row),
      data(row),
      created_at(row))
  }
}

abstract class ConcreteLicenses extends LicensesSack with RootConnector {

  override lazy val tableName = "licenses"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(l: LicensesResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, l.id)
      .value(_.data, l.data)
      .value(_.created_at, l.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def getRecord(id: String): ValidationNel[Throwable, Option[LicensesResult]] = {
    val res = select.allowFiltering().where(_.id eqs id).one()
    Await.result(res, 5.seconds).successNel
  }

}

object Licenses extends ConcreteLicenses {

  val FIRST_ID = "1"

  private def mkLicensesSack(input: String): ValidationNel[Throwable, Option[LicensesResult]] = {
    val ripNel: ValidationNel[Throwable, LicensesInput] = (Validation.fromTryCatchThrowable[models.admin.LicensesInput, Throwable] {
      parse(input).extract[LicensesInput]
    } leftMap { t: Throwable ⇒ new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
    for {
      rip ← ripNel
    } yield {
      val res = LicensesResult(FIRST_ID, rip.data, DateHelper.now())
      res.some
    }
  }

  def create(input: String): ValidationNel[Throwable, Option[LicensesResult]] = {
    for {
      wa ← (mkLicensesSack(input) leftMap { err: NonEmptyList[Throwable] ⇒ err })
      set ← (insertNewRecord(wa.get) leftMap { t: NonEmptyList[Throwable] ⇒ t })
    } yield {
      wa
    }
  }

  def findById(id: String): ValidationNel[Throwable, Option[LicensesResult]] = {
    val lid = id.some.getOrElse(FIRST_ID)

    play.api.Logger.debug(("%-20s -->[%s]").format("License id", lid))

    (getRecord(lid) leftMap { t: NonEmptyList[Throwable] ⇒
      new ServiceUnavailableError(id, (t.list.map(m ⇒ m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { xso: Option[LicensesResult] ⇒
      xso match {
        case Some(xs) ⇒ {
          Validation.success[Throwable, Option[LicensesResult]](xs.some).toValidationNel
        }
        case None ⇒ Validation.failure[Throwable, Option[LicensesResult]](new ResourceItemNotFound(id, "")).toValidationNel
      }
    }


  }

  implicit val sedimentLicenses = new Sedimenter[ValidationNel[Throwable, Option[LicensesResult]]] {
    def sediment(maybeASediment: ValidationNel[Throwable, Option[LicensesResult]]): Boolean = {
      maybeASediment.isSuccess
    }
  }

}
