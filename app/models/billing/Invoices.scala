/*
** Copyright [2013-2015] [Megam Systems]
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

package models.billing

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import cache._
import db._
import models.json.billing._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import app.MConfig

import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import org.megam.common.riak.GunnySack
import org.megam.util.Time
import org.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author ranjitha
 *
 */

case class InvoicesInput( from_date: String, to_date: String, month: String, bill_type: String, billing_amount: String, currency_type: String) {
  val json = "{\"from_date\":\"" + from_date + "\",\"to_date\":\"" + to_date + "\",\"month\":\"" + month + "\",\"bill_type\":\"" + bill_type + "\",\"billing_amount\":\"" + billing_amount + "\",\"currency_type\":\"" + currency_type + "\"\"}"

}

case class InvoicesResult(id: String, accounts_id: String, from_date: String, to_date: String, month: String, bill_type: String, billing_amount: String, currency_type: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.billing.InvoicesResultSerialization
    val preser = new InvoicesResultSerialization()
    toJSON(this)(preser.writer) //where does this JSON from?
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object InvoicesResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[InvoicesResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.billing.InvoicesResultSerialization
    val preser = new InvoicesResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[InvoicesResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Invoices {
  implicit val formats = DefaultFormats
  private val riak = GWRiak("invoices")
  val metadataKey = "Invoices"
  val metadataVal = "Invoices Creation"
  val bindex = "Invoices"

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to eventsinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
  val InvoicesInput: ValidationNel[Throwable, InvoicesInput] = (Validation.fromTryCatchThrowable[InvoicesInput, Throwable] {
      parse(input).extract[InvoicesInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      bhi <- InvoicesInput
      aor <- (models.base.Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "inv").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(aor.get.id)
      val json = new InvoicesResult(uir.get._1 + uir.get._2, aor.get.id, bhi.from_date, bhi.to_date, bhi.month, bhi.bill_type, bhi.billing_amount, bhi.currency_type, Time.now.toString).toJson(false)
      new GunnySack(uir.get._1 + uir.get._2, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new invoices for currently pay the bill of user.
   *
   */

  def create(email: String, input: String): ValidationNel[Throwable, Option[InvoicesResult]] = {
    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[InvoicesResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Invoices created. success",Console.RESET))
              (parse(gs.get.value).extract[InvoicesResult].some).successNel[Throwable];
            }
          }
        }
    }
  }

   def findByName(balanceList: Option[List[String]]): ValidationNel[Throwable, InvoicesResults] = {
    (balanceList map {
      _.map { balanceName =>
        play.api.Logger.debug("models.InvoicesName findByName: Invoices:" + balanceName)
        (riak.fetch(balanceName) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(balanceName, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              (Validation.fromTryCatchThrowable[models.billing.InvoicesResult,Throwable] {
                parse(xs.value).extract[InvoicesResult]
              } leftMap { t: Throwable =>
                new ResourceItemNotFound(balanceName, t.getMessage)
              }).toValidationNel.flatMap { j: InvoicesResult =>
                Validation.success[Throwable, InvoicesResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ?
              }
            }
            case None => Validation.failure[Throwable, InvoicesResults](new ResourceItemNotFound(balanceName, "")).toValidationNel
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((InvoicesResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head.

  }

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email,
   * the histories are listed on the index (account.id) in bucket `Invoices`.
   * Using a "Invoices name" as key, return a list of ValidationNel[List[InvoicesResult]]
   * Takes an email, and returns a Future[ValidationNel, List[Option[InvoicesResult]]]
   */
  def findByEmail(email: String): ValidationNel[Throwable, InvoicesResults] = {
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Throwable, InvoicesResults]] {
      (((for {
        aor <- (models.base.Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      } yield {
        val bindex = ""
        val bvalue = Set("")
         play.api.Logger.debug(("%-20s -->[%s]").format("Invoices result", aor.get))
        new GunnySack("Invoices", aor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
          None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
      }) leftMap { t: NonEmptyList[Throwable] => t } flatMap {
        gs: Option[GunnySack] => riak.fetchIndexByValue(gs.get)
      } map { nm: List[String] =>
        (if (!nm.isEmpty) findByName(nm.some) else
          new ResourceItemNotFound(email, "Invoice = nothing found.").failureNel[InvoicesResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    res.getOrElse(new ResourceItemNotFound(email, "Invoices = nothing found.").failureNel[InvoicesResults])
  }

}
