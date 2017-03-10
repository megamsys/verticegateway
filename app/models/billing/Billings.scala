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
import io.megam.auth.funnel.{ FunnelResponse, FunnelResponses }

import cache._
import db._
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import models.tosca.{ KeyValueField, KeyValueList}
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

case class BillingsInput(
                          key: String,
                          name: String,
                          allowed: KeyValueList,
                          inputs: KeyValueList,
                          quota_type: String,
                          status: String,
                          orderid: String,
                          gateway: String,
                          amount: String,
                          trandate: String,
                          currency_type: String) {}

object Billings {
  val HOSTING = "Hosting"
  val ITEM = "Item"
  val ADDFUNDS = "AddFunds"
  val OFFLINECC = "offlinecc"
  val PAID = "Paid"

  def create(email: String, input: String): ValidationNel[Throwable, Option[BillingsInput]] = {
    val billingsInput: ValidationNel[Throwable, BillingsInput] = (Validation.fromTryCatchThrowable[BillingsInput, Throwable] {
      parse(input).extract[BillingsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      balance <- billingsInput
      balUpdate <- atBalUpdate(email, balance)
      balDeduct <- atBalDeduct(email, balance)
      quotaUpdate <- atQuota(email, balance)
    } yield {
      balance.some
    }
  }

  def atQuota(email: String, balance: BillingsInput): ValidationNel[Throwable, Option[BillingsInput]] = {
    if(balance.key == HOSTING) {
        models.billing.Externalobjects.findById(email, balance.orderid) match {
          case Success(succ) => {
            atQuotaUpdate(email, balance, succ(0))
          }
          case Failure(err) => {
            val rn: FunnelResponse = new HttpReturningError(err)
            if(rn.code == 404) {
              atNewQuota(email, balance)
            } else {
              Validation.success[Throwable, Option[BillingsInput]](none).toValidationNel
            }
          }
        }
     } else {
       Validation.success[Throwable, Option[BillingsInput]](none).toValidationNel
     }
  }

  def atQuotaUpdate(email: String, balance: BillingsInput, exObjects: ExternalobjectsResult): ValidationNel[Throwable, Option[BillingsInput]] = {
    val quota_id = exObjects.inputs.find(_.key.equalsIgnoreCase("quota_id")).getOrElse(models.tosca.KeyValueField.empty).value.toString

    for {
      quota <- (models.billing.Quotas.findById(quota_id) leftMap { ut: NonEmptyList[Throwable] => ut })
      qpd <- (models.billing.Quotas.update(email, compactRender(Extraction.decompose(QuotasUpdateInput(quota_id, email, balance.allowed, quota(0).get.allocated_to, quota(0).get.inputs, quota(0).get.quota_type, balance.status)))) leftMap { ut: NonEmptyList[Throwable] => ut })
      qpdb <- (atQuotaPaidUpdate(email, balance, quota(0).get) leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      balance.some
    }

  }

  def atNewQuota(email: String, balance: BillingsInput): ValidationNel[Throwable, Option[BillingsInput]] = {
      val quota_input = QuotasInput(balance.name, email, balance.allowed, "", balance.inputs, balance.quota_type, balance.status)
      val exObject_input = ExternalobjectsInput(balance.orderid, balance.inputs)

      (models.billing.Quotas.create(email, compactRender(Extraction.decompose(quota_input))) leftMap { t: NonEmptyList[Throwable] =>
        new ServiceUnavailableError(balance.name, (t.list.map(m => m.getMessage)).mkString("\n"))
      }).toValidationNel.flatMap { xso: Option[QuotasResult] =>
        xso match {
          case Some(xs) => {
                val json = ExternalobjectsInput(balance.orderid, List(models.tosca.KeyValueField("quota_id", xs.id)))
                models.billing.Externalobjects.create(email, compactRender(Extraction.decompose(json))) match {
                  case Success(succ) => {
                    Validation.success[Throwable, Option[BillingsInput]](none).toValidationNel
                  }
                  case Failure(err) => {
                    Validation.failure[Throwable, Option[BillingsInput]](new ResourceItemNotFound(balance.name, "")).toValidationNel
                  }
                }
          }
          case None => {
            Validation.failure[Throwable, Option[BillingsInput]](new ResourceItemNotFound(balance.name, "")).toValidationNel
          }
        }
      }
  }

  def atQuotaPaidUpdate(email: String, balance: BillingsInput, quota: QuotasResult): ValidationNel[Throwable, Option[BillingsInput]] = {
    if(balance.status == PAID && balance.gateway == OFFLINECC) {
      val bal_input = BalancesUpdateInput("", balance.amount, DateHelper.now().toString(), DateHelper.now().toString())
      for {
        bal <- (models.billing.Balances.deduct_with_biller(email, compactRender(Extraction.decompose(bal_input))) leftMap { ut: NonEmptyList[Throwable] => ut })
      } yield {
        balance.some
      }
    } else{
      Validation.success[Throwable, Option[BillingsInput]](none).toValidationNel
    }
  }

  def atBalUpdate(email: String, balance: BillingsInput): ValidationNel[Throwable, Option[BillingsInput]] = {
    if(balance.key == ADDFUNDS) {
      val tran_input = models.billing.BillingtransactionsInput(balance.gateway, balance.amount, "0.00", "0.00", "", balance.trandate, balance.currency_type, balance.inputs)
      val bal_input = BalancesUpdateInput("", balance.amount, DateHelper.now().toString(), DateHelper.now().toString())
      for {
        billtran <- (models.billing.Billingtransactions.create(email, compactRender(Extraction.decompose(tran_input))) leftMap { ut: NonEmptyList[Throwable] => ut })
        bal <- (models.billing.Balances.update(email, compactRender(Extraction.decompose(bal_input))) leftMap { ut: NonEmptyList[Throwable] => ut })
        } yield {
          balance.some
        }
    } else {
      Validation.success[Throwable, Option[BillingsInput]](none).toValidationNel
    }
  }

  def atBalDeduct(email: String, balance: BillingsInput): ValidationNel[Throwable, Option[BillingsInput]] = {
    if(balance.key == ITEM && balance.gateway == OFFLINECC) {
      val bal_input = BalancesUpdateInput("", balance.amount, DateHelper.now().toString(), DateHelper.now().toString())
      for {
        bal <- (models.billing.Balances.deduct(email, compactRender(Extraction.decompose(bal_input))) leftMap { ut: NonEmptyList[Throwable] => ut })
        } yield {
          balance.some
        }
    } else {
      Validation.success[Throwable, Option[BillingsInput]](none).toValidationNel
    }
  }


}
