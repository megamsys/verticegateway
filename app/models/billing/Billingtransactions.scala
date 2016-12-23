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
import models.base._
import models.tosca.{ KeyValueField, KeyValueList}
import io.megam.auth.stack.AccountResult
import io.megam.auth.stack.{ Name, Phone, Password, States, Approval, Dates, Suspend }
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
 * @author rajesh
 *
 */

case class BillingtransactionsInput(gateway: String,
                                    amountin: String,
                                    amountout: String,
                                    fees: String,
                                    tranid: String,
                                    trandate: String,
                                    currency_type: String,
                                    inputs: models.tosca.KeyValueList
                                    )

case class BillingtransactionsResult(id: String,
                                    account_id: String,
                                    gateway: String,
                                    amountin: String,
                                    amountout: String,
                                    fees: String,
                                    tranid: String,
                                    trandate: String,
                                    currency_type: String,
                                    inputs: models.tosca.KeyValueList,
                                    json_claz: String,
                                    created_at: DateTime)

sealed class BillingtransactionsSacks extends CassandraTable[BillingtransactionsSacks, BillingtransactionsResult] with ImplicitJsonFormats {

  object id extends StringColumn(this)
  object account_id extends StringColumn(this) with PartitionKey[String]
  object gateway extends StringColumn(this)
  object amountin extends StringColumn(this)
  object amountout extends StringColumn(this)
  object fees extends StringColumn(this)
  object tranid extends StringColumn(this)
  object trandate extends StringColumn(this) with PrimaryKey[String]
  object currency_type extends StringColumn(this)

  object inputs extends JsonListColumn[BillingtransactionsSacks, BillingtransactionsResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object json_claz extends StringColumn(this)
  object created_at extends DateTimeColumn(this) with PrimaryKey[DateTime]


  def fromRow(row: Row): BillingtransactionsResult = {
    BillingtransactionsResult(
      id(row),
      account_id(row),
      gateway(row),
      amountin(row),
      amountout(row),
      fees(row),
      tranid(row),
      trandate(row),
      currency_type(row),
      inputs(row),
      json_claz(row),
      created_at(row))
  }
}

abstract class ConcreteBillingtransactions extends BillingtransactionsSacks with RootConnector {

  override lazy val tableName = "billingtransactions"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(ams: BillingtransactionsResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, ams.id)
      .value(_.account_id, ams.account_id)
      .value(_.gateway, ams.gateway)
      .value(_.amountin, ams.amountin)
      .value(_.amountout, ams.amountout)
      .value(_.fees, ams.fees)
      .value(_.tranid, ams.tranid)
      .value(_.trandate, ams.trandate)
      .value(_.currency_type, ams.currency_type)
      .value(_.inputs, ams.inputs)
      .value(_.json_claz, ams.json_claz)
      .value(_.created_at, ams.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(id: String): ValidationNel[Throwable, Seq[BillingtransactionsResult]] = {
    val res = select.allowFiltering().where(_.account_id eqs id).limit(20).fetch()
    Await.result(res, 5.seconds).successNel
  }

}

object Billingtransactions extends ConcreteBillingtransactions {

  private def mkBillingtransactionsSack(email: String, input: String): ValidationNel[Throwable, BillingtransactionsResult] = {
    val billInput: ValidationNel[Throwable, BillingtransactionsInput] = (Validation.fromTryCatchThrowable[BillingtransactionsInput, Throwable] {
      parse(input).extract[BillingtransactionsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      bill <- billInput
      set <- (atBalUpdate(email, bill.amountin, bill.inputs) leftMap { s: NonEmptyList[Throwable] => s })
      uir <- (UID("bhs").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(email)
      val json = new BillingtransactionsResult(uir.get._1 + uir.get._2, email, bill.gateway, bill.amountin, bill.amountout, bill.fees, bill.tranid, bill.trandate, bill.currency_type, bill.inputs, "Megam::BillingTransactions",DateHelper.now())
      json
    }
  }

  /*
   * create new billing transcations for currently pay the bill of user.
   *
   */
  def create(email: String, input: String): ValidationNel[Throwable, Option[BillingtransactionsResult]] = {
    for {
      wa <- (mkBillingtransactionsSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
      acc <- (atAccUpdate(email) leftMap { s: NonEmptyList[Throwable] => s })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Billingtransactions.created success", Console.RESET))
      wa.some
    }
  }

 def atAccUpdate(email: String): ValidationNel[Throwable,Option[AccountResult]] = {
 val approval = Approval("true", "", "")
 val  acc = AccountResult("", Name.empty, Phone.empty, email, new String(), Password.empty, States.empty, approval, Suspend.empty, new String(), Dates.empty)
 models.base.Accounts.update(email, compactRender(Extraction.decompose(acc)))
 }

 def atBalUpdate(email: String, amount: String, inputs: models.tosca.KeyValueList): ValidationNel[Throwable, BalancesResults] = {

  val quotaOpt = inputs.find(_.key.equalsIgnoreCase("quota_based")).toBoolean.some

for {
  quota <- quotaOpt
 } yield {
  val bal = BalancesResult("",email,amount,"", DateHelper.now(), DateHelper.now())
   if  (quota.value == "false") {
    models.billing.Balances.update(email, compactRender(Extraction.decompose(bal)))
   } else {
    bal
   }
 }
}

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email,
   * the transcations are listed on the index (account.id) in bucket `Billingtransactions`.
   * Using a "Billingtransactions name" as key, r      wa <- (msSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
return a list of ValidationNel[List[BillinghistoriesResult]]
   * Takes an email, and returns a Future[ValidationNel, List[Option[BillingtransactionsResult]]]
   */
  def findByEmail(email: String): ValidationNel[Throwable, Seq[BillingtransactionsResult]] = {
    (listRecords(email) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(email, "Billingtransactions = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[BillingtransactionsResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[BillingtransactionsResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[BillingtransactionsResult]](new ResourceItemNotFound(email, "Billingtransactions = nothing found.")).toValidationNel
    }

  }

}
