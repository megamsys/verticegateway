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
import io.megam.util.Time

import cache._
import db._
import models.base.Accounts
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._
import app.MConfig

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._

import io.megam.auth.funnel.FunnelErrors._
import app.MConfig
import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

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
    json_claz: String,
    created_at: String,
    updated_at: String) {
}

sealed class BalancesSacks extends CassandraTable[BalancesSacks, BalancesResult] {

  implicit val formats = DefaultFormats

  object id extends StringColumn(this) with PrimaryKey[String]
  object account_id extends StringColumn(this) with PartitionKey[String]
  object credit extends StringColumn(this)
  object json_claz extends StringColumn(this)
  object created_at extends StringColumn(this)
  object updated_at extends StringColumn(this)

  def fromRow(row: Row): BalancesResult = {
    BalancesResult(
      id(row),
      account_id(row),
      credit(row),
      json_claz(row),
      created_at(row),
      updated_at(row))
  }
}

abstract class ConcreteBalances extends BalancesSacks with RootConnector {
  // you can even rename the table in the schema to whatever you like.
  override lazy val tableName = "balances"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(ams: BalancesResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, ams.id)
      .value(_.account_id, ams.account_id)
      .value(_.credit, ams.credit)
      .value(_.json_claz, ams.json_claz)
      .value(_.created_at, ams.created_at)
      .value(_.updated_at, ams.updated_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def updateRecord(email: String, rip: BalancesResult, aor: Option[BalancesResult]): ValidationNel[Throwable, ResultSet] = {
    val oldbal = aor.get.credit.toFloat
    val newbal = rip.credit.toFloat
    val updatecredit = oldbal + newbal
    val res = update.where(_.account_id eqs email)
      .modify(_.credit setTo NilorNot(updatecredit.toString, aor.get.credit))
      .and(_.updated_at setTo Time.now.toString)
      .future()
      println(res)
      Await.result(res, 5.seconds).successNel
  }

  def getRecord(id: String): ValidationNel[Throwable, Option[BalancesResult]] = {
    val res = select.allowFiltering().where(_.account_id eqs id).one()
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

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to eventsinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkBalancesSack(email: String, input: String): ValidationNel[Throwable, BalancesResult] = {
    val balancesInput: ValidationNel[Throwable, BalancesInput] = (Validation.fromTryCatchThrowable[BalancesInput, Throwable] {
      parse(input).extract[BalancesInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      balance <- balancesInput
      uir <- (UID("bal").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {

      val bvalue = Set(email)
      val json = new BalancesResult(uir.get._1 + uir.get._2, email, balance.credit, "Megam::Balances", Time.now.toString, Time.now.toString)
      json
    }
  }

  /*
   * create a new balance entry for the user.
   *
   */
  def create(email: String, input: String): ValidationNel[Throwable, Option[BalancesResult]] = {
    for {
      wa <- (mkBalancesSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Balances.created success", Console.RESET))
      wa.some
    }
  }

  def onboardAccountBalance(email: String): ValidationNel[Throwable, Option[BalancesResult]] = {
    for {
      wa <- (create(email, BalancesInput("0").json) leftMap { err: NonEmptyList[Throwable] => err })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Account Balance onboard. success", Console.RESET))
      wa
    }
  }

  def update(email: String, input: String): ValidationNel[Throwable, BalancesResults] = {
    val ripNel: ValidationNel[Throwable, BalancesResult] = (Validation.fromTryCatchThrowable[BalancesResult,Throwable] {
      parse(input).extract[BalancesResult]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
    for {
      rip <- ripNel
      bor <- (Balances.findByEmail(List(email).some) leftMap { t: NonEmptyList[Throwable] => t })
      set <- updateRecord(email, rip, bor.head)
    } yield {
      bor
    }
  }

  def findByEmail(balancesID: Option[List[String]]): ValidationNel[Throwable, BalancesResults] = {
    (balancesID map {
      _.map { asm_id =>
        play.api.Logger.debug(("%-20s -->[%s]").format("Balances Id", asm_id))
        (getRecord(asm_id) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(asm_id, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[BalancesResult] =>
          xso match {
            case Some(xs) => {
              play.api.Logger.debug(("%-20s -->[%s]").format("BalancesResult", xs))
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


}
