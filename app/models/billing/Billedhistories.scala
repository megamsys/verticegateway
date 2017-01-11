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
 * @author rajthilak
 *
 */
case class BilledhistoriesInput(assembly_id: String,
                                bill_type: String,
                                billing_amount: String,
                                currency_type: String,
                                start_date: String,
                                end_date: String)

case class BilledhistoriesResult(
                                id: String,
                                account_id: String,
                                assembly_id: String,
                                bill_type: String,
                                billing_amount: String,
                                currency_type: String,
                                start_date:  DateTime,
                                end_date: DateTime,
                                json_claz: String,
                                created_at: DateTime)

sealed class BilledhistoriesSacks extends CassandraTable[BilledhistoriesSacks, BilledhistoriesResult]  with ImplicitJsonFormats{

  object id extends StringColumn(this)
  object account_id extends StringColumn(this) with PartitionKey[String]
  object assembly_id extends StringColumn(this) with PrimaryKey[String]
  object bill_type extends StringColumn(this) with PrimaryKey[String]
  object billing_amount extends StringColumn(this)
  object currency_type extends StringColumn(this)
  object start_date extends DateTimeColumn(this)
  object end_date extends DateTimeColumn(this)
  object json_claz extends StringColumn(this)
  object created_at extends DateTimeColumn(this) with PrimaryKey[DateTime]

  def fromRow(row: Row): BilledhistoriesResult = {
    BilledhistoriesResult(
      id(row),
      account_id(row),
      assembly_id(row),
      bill_type(row),
      billing_amount(row),
      currency_type(row),
      start_date(row),
      end_date(row),
      json_claz(row),
      created_at(row))
  }
}

abstract class ConcreteBilledhistories extends BilledhistoriesSacks with RootConnector {

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
      .value(_.start_date, ams.start_date)
      .value(_.end_date, ams.end_date)
      .value(_.json_claz, ams.json_claz)
      .value(_.created_at, ams.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(id: String): ValidationNel[Throwable, Seq[BilledhistoriesResult]] = {
    val res = select.where(_.account_id eqs id).fetch()
    Await.result(res, 5.seconds).successNel
  }

  def dateRangeBy(startdate: String, enddate: String): ValidationNel[Throwable, Seq[BilledhistoriesResult]] = {
      val starttime = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).parseDateTime(startdate);
      val endtime = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).parseDateTime(enddate);

     val res = select.allowFiltering().where(_.created_at gte starttime).and(_.created_at lte endtime).fetch()
    Await.result(res, 5.seconds).successNel
  }

  def dateRangeFor(email: String, startdate: String, enddate: String): ValidationNel[Throwable, Seq[BilledhistoriesResult]] = {
     val starttime = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).parseDateTime(startdate);
     val endtime = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).parseDateTime(enddate);

     val res = select.allowFiltering().where(_.account_id eqs email).and(_.created_at gte starttime).and(_.created_at lte endtime).fetch()
       Await.result(res, 5.seconds).successNel
   }

}

object Billedhistories extends ConcreteBilledhistories {

    private def mkBilledhistoriesSack(email: String, input: String): ValidationNel[Throwable, BilledhistoriesResult] = {
      val billInput: ValidationNel[Throwable, BilledhistoriesInput] = (Validation.fromTryCatchThrowable[BilledhistoriesInput, Throwable] {
      parse(input).extract[BilledhistoriesInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      bill <- billInput
      uir <- (UID("bhs").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(email)
      val json = new BilledhistoriesResult(uir.get._1 + uir.get._2, email, bill.assembly_id, bill.bill_type, bill.billing_amount, bill.currency_type, DateTime.parse(bill.start_date), DateTime.parse(bill.end_date), "Megam::Billedhistories", DateHelper.now())
      json
    }
  }


  def create(email: String, input: String): ValidationNel[Throwable, Option[BilledhistoriesResult]] = {
    for {
      wa <- (mkBilledhistoriesSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Billedhistories.created success", Console.RESET))
      wa.some
    }
  }


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

  def findByDateRange(startdate: String, enddate: String): ValidationNel[Throwable, Seq[BilledhistoriesResult]] = {
    dateRangeBy(startdate, enddate) match {
      case Success(value) => Validation.success[Throwable, Seq[BilledhistoriesResult]](value).toValidationNel
      case Failure(err) => Validation.success[Throwable, Seq[BilledhistoriesResult]](List()).toValidationNel
    }
  }

  def findByDateRangeFor(email: String, startdate: String, enddate: String): ValidationNel[Throwable, Seq[BilledhistoriesResult]] = {
    dateRangeFor(email, startdate, enddate) match {
      case Success(value) => Validation.success[Throwable, Seq[BilledhistoriesResult]](value).toValidationNel
      case Failure(err) => Validation.success[Throwable, Seq[BilledhistoriesResult]](List()).toValidationNel
    }
  }
}
