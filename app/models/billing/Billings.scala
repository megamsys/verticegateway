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
import controllers.funnel.FunnelErrors._

import cache._
import db._
import models.base._
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
 * @author rajthilak
 *
 */

case class BillingsInput(accounts_id: String, line1: String, line2: String, country_code: String, postal_code: String, state: String, phone: String, bill_type: String) {
  val json = "{\"accounts_id\":\"" + accounts_id + "\",\"line1\":\"" + line1 + "\",\"line2\":\"" + line2 + "\",\"country_code\":\"" + country_code + "\",\"postal_code\":\"" + postal_code + "\",\"state\":\"" + state + "\",\"phone\":\"" + phone + "\",\"bill_type\":\"" + bill_type + "\"}"

}

case class BillingsResult(id: String, accounts_id: String, line1: String, line2: String, country_code: String, postal_code: String, state: String, phone: String, bill_type: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.billing.BillingsResultSerialization
    val preser = new BillingsResultSerialization()
    toJSON(this)(preser.writer) //where does this JSON from?
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object BillingsResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[BillingsResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.billing.BillingsResultSerialization
    val preser = new BillingsResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[BillingsResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue,Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Billings {
  implicit val formats = DefaultFormats
  private val riak = GWRiak("billings")
  val metadataKey = "Billings"
  val metadataVal = "Billings Creation"
  val bindex = "Billings"

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to eventsinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    val BillingsInput: ValidationNel[Throwable, BillingsInput] = (Validation.fromTryCatchThrowable[BillingsInput,Throwable] {
      parse(input).extract[BillingsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      billing <- BillingsInput
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "bil").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(billing.accounts_id)
      val json = new BillingsResult(uir.get._1 + uir.get._2, billing.accounts_id, billing.line1, billing.line2, billing.country_code, billing.postal_code, billing.state, billing.phone, billing.bill_type, Time.now.toString).toJson(false)
      new GunnySack(uir.get._1 + uir.get._2, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new billing entry for user information.
   * Also this is contain billing type and currency type of user used to pay the bill
   */

  def create(email: String, input: String): ValidationNel[Throwable, Option[BillingsResult]] = {
    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[BillingsResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD,"Billings.created successfully",Console.RESET))
              (parse(gs.get.value).extract[BillingsResult].some).successNel[Throwable];
            }
          }
        }
    }
  }

}
