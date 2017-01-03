package models.billing

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps
import scala.collection.mutable.ListBuffer

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
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import controllers.stack.ImplicitJsonFormats


/**
 * @author ranjitha
 *
 */

case class CreditsInput(account_id: String, credit: String) {
  val json = "{\"account_id\":\"" + account_id + "\",\"credit\":\"" + credit + "\"}"

}

case class CreditsResult(
    id: String,
    account_id: String,
    credit: String,
    json_claz: String,
    created_at: DateTime) {
}

sealed class CreditsSacks extends CassandraTable[CreditsSacks, CreditsResult] with ImplicitJsonFormats {

  object id extends StringColumn(this)
  object account_id extends StringColumn(this) with PartitionKey[String]
  object credit extends StringColumn(this)
  object json_claz extends StringColumn(this)
  object created_at extends DateTimeColumn(this) with PrimaryKey[DateTime]

  def fromRow(row: Row): CreditsResult = {
    CreditsResult(
      id(row),
      account_id(row),
      credit(row),
      json_claz(row),
      created_at(row))
  }
}

abstract class ConcreteCredits extends CreditsSacks with RootConnector {

  override lazy val tableName = "credits"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(ams: CreditsResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, ams.id)
      .value(_.account_id, ams.account_id)
      .value(_.credit, ams.credit)
      .value(_.json_claz, ams.json_claz)
      .value(_.created_at, ams.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def getRecords(account_id: String): ValidationNel[Throwable, Seq[CreditsResult]] = {
    val res = select.allowFiltering().where(_.account_id eqs account_id).fetch()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(): ValidationNel[Throwable, Seq[CreditsResult]] = {
    val res = select.fetch()
    Await.result(res, 5.seconds).successNel
  }

}

object Credits extends ConcreteCredits{


  private def mkCreditsSack(email: String, input: String): ValidationNel[Throwable, CreditsResult] = {
    val creditsInput: ValidationNel[Throwable, CreditsInput] = (Validation.fromTryCatchThrowable[CreditsInput, Throwable] {
      parse(input).extract[CreditsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
    println(creditsInput)
    for {
      cr <- creditsInput
      uir <- (UID("cr").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {

      val bvalue = Set(email)
      val json = new CreditsResult(uir.get._1 + uir.get._2, cr.account_id, cr.credit, "Megam::Credits", DateHelper.now())
      json
    }
  }


  def create(email: String, input: String): ValidationNel[Throwable, Option[CreditsResult]] = {
    for {
      wa <- (mkCreditsSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Credits.created success", Console.RESET))
      wa.some
    }
  }

  def list: ValidationNel[Throwable, Seq[CreditsResult]] = {
    (listRecords() leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound("", "Credits = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[CreditsResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[CreditsResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[CreditsResult]](new ResourceItemNotFound("", "Credits = nothing found.")).toValidationNel
    }

  }

  def findById(email: String): ValidationNel[Throwable, Seq[CreditsResult]] = {
    play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Credits " + email, Console.RESET))

    (getRecords(email) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(email, "Credits = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[CreditsResult] =>
     if (!nm.isEmpty)
        Validation.success[Throwable, Seq[CreditsResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[CreditsResult]](new ResourceItemNotFound(email, "Credits = nothing found.")).toValidationNel
    }
  }


}
