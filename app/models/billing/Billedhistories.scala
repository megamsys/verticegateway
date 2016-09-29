package models.billing

import scalaz._
import Scalaz._
import scalaz.effect.IO
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

import io.megam.util.Time
import app.MConfig
import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author rajthilak
 *
 */

case class BilledhistoriesInput(assembly_id: String, bill_type: String, billing_amount: String, currency_type: String) {

}

case class BilledhistoriesResult(
    id: String,
    account_id: String,
    assembly_id: String,
    bill_type: String,
    billing_amount: String,
    currency_type: String,
    json_claz: String,
    created_at: String) {
}

sealed class BilledhistoriesSacks extends CassandraTable[BilledhistoriesSacks, BilledhistoriesResult] {

  implicit val formats = DefaultFormats

  object id extends StringColumn(this) with PrimaryKey[String]
  object account_id extends StringColumn(this) with PartitionKey[String]
  object assembly_id extends StringColumn(this)
  object bill_type extends StringColumn(this)
  object billing_amount extends StringColumn(this)
  object currency_type extends StringColumn(this)
  object json_claz extends StringColumn(this)
  object created_at extends StringColumn(this)

  def fromRow(row: Row): BilledhistoriesResult = {
    BilledhistoriesResult(
      id(row),
      account_id(row),
      assembly_id(row),
      bill_type(row),
      billing_amount(row),
      currency_type(row),
      json_claz(row),
      created_at(row))
  }
}

abstract class ConcreteBilledhistories extends BilledhistoriesSacks with RootConnector {
  // you can even rename the table in the schema to whatever you like.
  override lazy val tableName = "billedhistories"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(ams: BilledhistoriesResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, ams.id)
      .value(_.account_id, ams.account_id)
      .value(_.assembly_id, ams.assembly_id)
      .value(_.bill_type, ams.bill_type)
      .value(_.billing_amount, ams.billing_amount)
      .value(_.currency_type, ams.currency_type)
      .value(_.json_claz, ams.json_claz)
      .value(_.created_at, ams.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(id: String): ValidationNel[Throwable, Seq[BilledhistoriesResult]] = {
    val res = select.where(_.account_id eqs id).fetch()
    Await.result(res, 5.seconds).successNel
  }

}

object Billedhistories extends ConcreteBilledhistories {

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to eventsinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkBilledhistoriesSack(email: String, input: String): ValidationNel[Throwable, BilledhistoriesResult] = {
    val billInput: ValidationNel[Throwable, BilledhistoriesInput] = (Validation.fromTryCatchThrowable[BilledhistoriesInput, Throwable] {
      parse(input).extract[BilledhistoriesInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      bill <- billInput
      uir <- (UID("bhs").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {

      val bvalue = Set(email)
      val json = new BilledhistoriesResult(uir.get._1 + uir.get._2, email, bill.assembly_id, bill.bill_type, bill.billing_amount, bill.currency_type, "Megam::Billedhistories", Time.now.toString)
      json
    }
  }

  /*
   * create new billing histories for currently pay the bill of user.
   *
   */
  def create(email: String, input: String): ValidationNel[Throwable, Option[BilledhistoriesResult]] = {
    for {
      wa <- (mkBilledhistoriesSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Billedhistories.created success", Console.RESET))
      wa.some
    }
  }

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email,
   * the histories are listed on the index (account.id) in bucket `Billinghistories`.
   * Using a "Billinghistories name" as key, return a list of ValidationNel[List[BillinghistoriesResult]]
   * Takes an email, and returns a Future[ValidationNel, List[Option[BillinghistoriesResult]]]
   */
  def findByEmail(email: String): ValidationNel[Throwable, Seq[BilledhistoriesResult]] = {
    (listRecords(email) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(email, "Billedhistories = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[BilledhistoriesResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[BilledhistoriesResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[BilledhistoriesResult]](new ResourceItemNotFound(email, "Billedhistories = nothing found.")).toValidationNel
    }

  }

}
