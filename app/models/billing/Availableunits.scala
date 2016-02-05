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
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._
import app.MConfig

import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import io.megam.common.riak.GunnySack
import io.megam.common.uid.UID
import io.megam.util.Time

import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author rajthilak
 *
 */

case class AvailableunitsInput(name: String, duration: String, charges_per_duration: String) {
  val json = "{\"name\":\"" + name + "\",\"duration\":\"" + duration + "\",\"charges_per_duration\":\"" + charges_per_duration + "\"}"

}

case class AvailableunitsResult(id: String, name: String, duration: String, charges_per_duration: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.billing.AvailableunitsResultSerialization
    val preser = new AvailableunitsResultSerialization()
    toJSON(this)(preser.writer) //where does this JSON from?
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object AvailableunitsResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[AvailableunitsResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.billing.AvailableunitsResultSerialization
    val preser = new AvailableunitsResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[AvailableunitsResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Availableunits {
  implicit val formats = DefaultFormats

  private lazy val bucker = "availableunits"

  private lazy val riak = GWRiak(bucker)

  private lazy val idxedBy = idxAccountsId

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to eventsinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    val AvailableunitsInput: ValidationNel[Throwable, AvailableunitsInput] = (Validation.fromTryCatchThrowable[AvailableunitsInput, Throwable] {
      parse(input).extract[AvailableunitsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      aui <- AvailableunitsInput
      uir <- (UID("uts").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(uir.get._1 + uir.get._2)
      val json = new AvailableunitsResult(uir.get._1 + uir.get._2, aui.name, aui.duration, aui.charges_per_duration, Time.now.toString).toJson(false)
      new GunnySack(uir.get._1 + uir.get._2, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map.empty, Map((idxedBy, bvalue))).some
    }
  }

  /*
   * create new static units for seperate item with the 'name' of the item provide as input.
   * Also creating index with 'static units'
   */

  def create(email: String, input: String): ValidationNel[Throwable, Option[AvailableunitsResult]] = {
    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[AvailableunitsResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Availableunits.created success", Console.RESET))
              (parse(gs.get.value).extract[AvailableunitsResult].some).successNel[Throwable];
            }
          }
        }
    }
  }

}
