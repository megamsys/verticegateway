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
 * @author rajthilak
 *
 */

case class BalancesInput(credit: String) {
  val json = "{\"credit\":\"" + credit + "\"}"

}

case class BalancesUpdateInput(id: String, credit: String, created_at: String, updated_at: String) {
  val json = "{\"id\":\"" + id + "\",\"credit\":\"" + credit + "\",\"created_at\":\"" + created_at + "\",\"updated_at\":\"" + updated_at + "\"}"

}

case class BalancesResult(
    id: String,
    account_id: String,
    credit: String,
    biller_credit: String,
    json_claz: String,
    created_at: DateTime,
    updated_at: DateTime) {
}

sealed class BalancesSacks extends CassandraTable[BalancesSacks, BalancesResult] with ImplicitJsonFormats {

  object id extends StringColumn(this) with PrimaryKey[String]
  object account_id extends StringColumn(this) with PartitionKey[String]
  object credit extends StringColumn(this)
  object biller_credit extends StringColumn(this)
  object json_claz extends StringColumn(this)
  object created_at extends DateTimeColumn(this)
  object updated_at extends DateTimeColumn(this)

  def fromRow(row: Row): BalancesResult = {
    BalancesResult(
      id(row),
      account_id(row),
      credit(row),
      biller_credit(row),
      json_claz(row),
      created_at(row),
      updated_at(row))
  }
}

abstract class ConcreteBalances extends BalancesSacks with RootConnector {

  override lazy val tableName = "balances"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(ams: BalancesResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, ams.id)
      .value(_.account_id, ams.account_id)
      .value(_.credit, ams.credit)
      .value(_.biller_credit, ams.biller_credit)
      .value(_.json_claz, ams.json_claz)
      .value(_.created_at, ams.created_at)
      .value(_.updated_at, ams.updated_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def creditRecord(email: String, rip: BalancesResult, aor: Option[BalancesResult]): ValidationNel[Throwable, ResultSet] = {
    val oldbal = aor.get.credit.toFloat
    val oldbilbal = aor.get.biller_credit.toFloat
    val newbal = rip.credit.toFloat
    val updatecredit = oldbal + newbal
    val updatebillercredit = oldbilbal + newbal
    val res = update.where(_.account_id eqs email)
      .modify(_.credit setTo NilorNot(updatecredit.toString, aor.get.credit))
      .and(_.biller_credit setTo NilorNot(updatebillercredit.toString, aor.get.biller_credit))
      .and(_.updated_at setTo DateHelper.now())
      .future()
      Await.result(res, 5.seconds).successNel
  }

  def creditDeductRecord(email: String, rip: BalancesResult, aor: Option[BalancesResult]): ValidationNel[Throwable, ResultSet] = {
    val oldbilbal = aor.get.biller_credit.toFloat
    val newbal = rip.credit.toFloat
    val updatebillercredit = oldbilbal - newbal
    val res = update.where(_.account_id eqs email)
      .modify(_.biller_credit setTo NilorNot(updatebillercredit.toString, aor.get.biller_credit))
      .and(_.updated_at setTo DateHelper.now())
      .future()
      Await.result(res, 5.seconds).successNel
  }

  def creditDeductWithBillerRecord(email: String, rip: BalancesResult, aor: Option[BalancesResult]): ValidationNel[Throwable, ResultSet] = {
    val oldbal = aor.get.credit.toFloat
    val oldbilbal = aor.get.biller_credit.toFloat
    val newbal = rip.credit.toFloat
    val updatecredit = oldbal - newbal
    val updatebillercredit = oldbilbal - newbal
    val res = update.where(_.account_id eqs email)
      .modify(_.credit setTo NilorNot(updatecredit.toString, aor.get.credit))
      .and(_.biller_credit setTo NilorNot(updatebillercredit.toString, aor.get.biller_credit))
      .and(_.updated_at setTo DateHelper.now())
      .future()
      Await.result(res, 5.seconds).successNel
  }

  def updateRecord(email: String, rip: BalancesResult): ValidationNel[Throwable, ResultSet] = {
    val res = update.where(_.account_id eqs email)
      .modify(_.credit setTo rip.credit)
      .and(_.updated_at setTo DateHelper.now())
      .future()
      Await.result(res, 5.seconds).successNel
  }

  def getRecord(id: String): ValidationNel[Throwable, Option[BalancesResult]] = {
    val res = select.allowFiltering().where(_.account_id eqs id).one()
    Await.result(res, 5.seconds).successNel
  }

  def deleteRecords(email: String): ValidationNel[Throwable, ResultSet] = {
    val res = delete.where(_.account_id eqs email).future()
    Await.result(res, 5.seconds).successNel
  }

  def listAllRecords: ValidationNel[Throwable, Seq[BalancesResult]] = {
    val res = select.fetch
    Await.result(res, 5.seconds).successNel
  }


   def NilorNot(rip: String, bal: String): String = {
    rip == null match {
      case true => return bal
      case false => return rip
    }
  }
}

object Balances extends ConcreteBalances{


  private def mkBalancesSack(email: String, input: String): ValidationNel[Throwable, BalancesResult] = {
    val balancesInput: ValidationNel[Throwable, BalancesInput] = (Validation.fromTryCatchThrowable[BalancesInput, Throwable] {
      parse(input).extract[BalancesInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      balance <- balancesInput
      uir <- (UID("bal").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {

      val bvalue = Set(email)
      val json = new BalancesResult(uir.get._1 + uir.get._2, email, balance.credit, balance.credit, "Megam::Balances", DateHelper.now(), DateHelper.now())
      json
    }
  }


  def create(email: String, input: String): ValidationNel[Throwable, Option[BalancesResult]] = {
    for {
      wa <- (mkBalancesSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.GREEN, Console.BOLD, "Balances","|+| ✔", Console.RESET))
      wa.some
    }
  }

  def onboardAccountBalance(email: String): ValidationNel[Throwable, Option[BalancesResult]] = {
    for {
      wa <- (create(email, BalancesInput("0").json) leftMap { err: NonEmptyList[Throwable] => err })
    } yield {
      wa
    }
  }

  private def mkUpdateWithAmount(email: String, input: String): ValidationNel[Throwable, BalancesResult] = {
    val ripNel: ValidationNel[Throwable, BalancesUpdateInput] = (Validation.fromTryCatchThrowable[BalancesUpdateInput,Throwable] {
      parse(input).extract[BalancesUpdateInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
    for {
      bor <- ripNel
    } yield {
      val json = BalancesResult(bor.id, email, bor.credit, "",
         "", DateTime.parse(bor.created_at), DateTime.parse(bor.updated_at))
      json
    }
  }

  def deduct(email: String, input: String): ValidationNel[Throwable, BalancesResults] = {
    for {
      rip <- (mkUpdateWithAmount(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      bor <- (Balances.findByEmail(List(email).some) leftMap { t: NonEmptyList[Throwable] => t })
      set <- creditDeductRecord(email, rip, bor.head)
    } yield {
      bor
    }
  }

  def deduct_with_biller(email: String, input: String): ValidationNel[Throwable, BalancesResults] = {
    for {
      rip <- (mkUpdateWithAmount(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      bor <- (Balances.findByEmail(List(email).some) leftMap { t: NonEmptyList[Throwable] => t })
      set <- creditDeductWithBillerRecord(email, rip, bor.head)
    } yield {
      bor
    }
  }

  def update(email: String, input: String): ValidationNel[Throwable, BalancesResults] = {
    for {
      rip <- (mkUpdateWithAmount(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      bor <- (Balances.findByEmail(List(email).some) leftMap { t: NonEmptyList[Throwable] => t })
      set <- creditRecord(email, rip, bor.head)
    } yield {
      bor
    }
  }

  def update(input: String): ValidationNel[Throwable, BalancesResults] = {
    val ripNel: ValidationNel[Throwable, BalancesResult] = (Validation.fromTryCatchThrowable[BalancesResult,Throwable] {
      parse(input).extract[BalancesResult]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
    for {
      rip <- ripNel
      bor <- (Balances.findByEmail(List(rip.account_id).some) leftMap { t: NonEmptyList[Throwable] => t })
      set <- creditRecord(rip.account_id, rip, bor.head)
    } yield {
      bor
    }
  }

  private def updateBalancesSack(email: String, input: String): ValidationNel[Throwable, Option[BalancesResult]] = {
    val ripNel: ValidationNel[Throwable, BalancesUpdateInput] = (Validation.fromTryCatchThrowable[BalancesUpdateInput,Throwable] {
      parse(input).extract[BalancesUpdateInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      rip <- ripNel
      bor_collection <- (findByEmail(List(email).some) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      val bor = bor_collection.head
      val json = BalancesResult(bor.get.id, bor.get.account_id, rip.credit, "",
         bor.get.json_claz,
         bor.get.created_at,
         bor.get.updated_at)
      json.some
    }
  }


  def bill(email: String, input: String): ValidationNel[Throwable, Option[BalancesResult]] = {
    for {
      gs <- (updateBalancesSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (updateRecord(email, gs.get) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.YELLOW, Console.BOLD, "Balances","|÷| ✔", Console.RESET))
      gs
    }
  }

  def findByEmail(balancesID: Option[List[String]]): ValidationNel[Throwable, BalancesResults] = {
    (balancesID map {
      _.map { asm_id =>
        (getRecord(asm_id) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(asm_id, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[BalancesResult] =>
          xso match {
            case Some(xs) => {
              Validation.success[Throwable, BalancesResults](List(xs.some)).toValidationNel //screwy kishore, every element in a list ?
            }
            case None => {
              Validation.failure[Throwable, BalancesResults](new ResourceItemNotFound(asm_id, "")).toValidationNel
            }
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((BalancesResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head.
  }

  def delete(email: String): ValidationNel[Throwable, Option[BalancesResults]] = {
    deleteRecords(email) match {
      case Success(value) => Validation.success[Throwable, Option[BalancesResults]](none).toValidationNel
      case Failure(err) => Validation.success[Throwable, Option[BalancesResults]](none).toValidationNel
    }
  }


}
