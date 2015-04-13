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
import scalaz.syntax.SemigroupOps
import scalaz.NonEmptyList._
import scalaz.Validation._
import scalaz.effect.IO
import scalaz.EitherT._
import org.megam.util.Time
import Scalaz._
import controllers.stack._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import models.billing._
import models.cache._
import models.riak._
import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author rajthilak
 *
 */

case class DiscountsInput(account_id: String, bill_type: String, code: String, status: String) {
  val json = "{\"account_id\":\"" + account_id + "\",\"bill_type\":\"" + bill_type + "\",\"code\":\"" + code + "\",\"status\":\"" + status + "\"}"

}

case class DiscountsResult(id: String, account_id: String, bill_type: String, code: String, status: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.billing.DiscountsResultSerialization
    val preser = new DiscountsResultSerialization()
    toJSON(this)(preser.writer) //where does this JSON from? 
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object DiscountsResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[DiscountsResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.billing.DiscountsResultSerialization
    val preser = new DiscountsResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[DiscountsResult] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Discounts {
  implicit val formats = DefaultFormats
  private val riak = GWRiak("discounts")

  //implicit def EventsResultsSemigroup: Semigroup[EventsResults] = Semigroup.instance((f1, f2) => f1.append(f2))
  
  
  val metadataKey = "Discounts"
  val metadataVal = "Discounts Creation"
  val bindex = "Discounts"

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to eventsinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.billing.Discounts", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val DiscountsInput: ValidationNel[Throwable, DiscountsInput] = (Validation.fromTryCatch {
      parse(input).extract[DiscountsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      discount <- DiscountsInput
      //aor <- (models.Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "dst").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //val bvalue = Set(aor.get.id)
       val bvalue = Set(discount.account_id)
      val json = new DiscountsResult(uir.get._1 + uir.get._2, discount.account_id, discount.bill_type, discount.code, discount.status, Time.now.toString).toJson(false)
      new GunnySack(uir.get._1 + uir.get._2, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new events item with the 'name' of the item provide as input.
   * Also creating index with 'events'
   */

  def create(email: String, input: String): ValidationNel[Throwable, Option[DiscountsResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Discounts", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[DiscountsResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("Discounts created. success", "Scaliak returned => None. Thats OK."))
              (parse(gs.get.value).extract[DiscountsResult].some).successNel[Throwable];
            }
          }
        }
    }
  }
  
}

