/*
** Copyright [2013-2016] [Megam Systems]
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
import io.megam.auth.funnel.FunnelErrors._
import app.MConfig

import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import io.megam.common.riak.GunnySack
import io.megam.util.Time
import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author rajthilak
 *
 */

case class SubscriptionsInput(accounts_id: String, assembly_id: String, start_date: String, end_date: String) {
  val json = "{\"start_date\":\"" + start_date + "\",\"end_date\":\"" + end_date + "\"}"

}

case class SubscriptionsResult(id: String, accounts_id: String, assembly_id: String, start_date: String, end_date: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.billing.SubscriptionsResultSerialization
    val preser = new SubscriptionsResultSerialization()
    toJSON(this)(preser.writer) //where does this JSON from?
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object SubscriptionsResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[SubscriptionsResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.billing.SubscriptionsResultSerialization
    val preser = new SubscriptionsResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[SubscriptionsResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Subscriptions {
  implicit val formats = DefaultFormats
  private val riak = GWRiak("subscriptions")

  val metadataKey = "Subscriptions"
  val metadataVal = "Subscriptions Creation"
  val bindex = "Subscriptions"

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to eventsinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    val SubscriptionsInput: ValidationNel[Throwable, SubscriptionsInput] = (Validation.fromTryCatchThrowable[SubscriptionsInput,Throwable] {
      parse(input).extract[SubscriptionsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      sub <- SubscriptionsInput
      //aor <- (models.base.Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      uir <- (UID("sub").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //val bvalue = Set(aor.get.id)
      val bvalue = Set(sub.accounts_id)
      val json = new SubscriptionsResult(uir.get._1 + uir.get._2, sub.accounts_id, sub.assembly_id, sub.start_date, sub.end_date, Time.now.toString).toJson(false)
      new GunnySack(uir.get._1 + uir.get._2, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /**
   * create new subscriptions for user with account id and virtual machine id.
   * This was store the starting time and ending time of virtual machines
   **/

  def create(email: String, input: String): ValidationNel[Throwable, Option[SubscriptionsResult]] = {
    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[SubscriptionsResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Subscriptions created. success", Console.RESET))
              (parse(gs.get.value).extract[SubscriptionsResult].some).successNel[Throwable];
            }
          }
        }
    }
  }

}
