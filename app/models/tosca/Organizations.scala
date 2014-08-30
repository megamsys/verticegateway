/* 
** Copyright [2012-2014] [Megam Systems]
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

package models.tosca

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
import models._
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
 * @author morpheyesh
 *
 */

case class OrganizationsInput(name: String) {
  val json = "{\"name\":\"" + name + "\"}"
}

case class OrganizationsResult(id: String, name: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.OrganizationsResultSerialization
    val preser = new OrganizationsResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object OrganizationsResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[OrganizationsResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.OrganizationsResultSerialization
    val preser = new OrganizationsResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[OrganizationsResult] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Organizations {
  implicit val formats = DefaultFormats
  private val riak = GWRiak("organizations")

  val metadataKey = "Organizations"
  val metadataVal = "Organizations Creation"
  val bindex = "organization"

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to organizationsinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.tosca.Organizations", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val organizationsInput: ValidationNel[Throwable, OrganizationsInput] = (Validation.fromTryCatch {
      parse(input).extract[OrganizationsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      mkp <- organizationsInput
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "mkp").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(mkp.name)
      val json = new OrganizationsResult(uir.get._1 + uir.get._2, mkp.name, Time.now.toString).toJson(false)
      new GunnySack(mkp.name, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }
 
  /*
   * create new organization item with the 'name' of the item provide as input.
   * A index name organization name will point to the "organization bucket" bucket.
   */

  def create(email: String, input: String): ValidationNel[Throwable, Option[OrganizationsResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Organizations", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[OrganizationsResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("Organizations.created success", "Scaliak returned => None. Thats OK."))
              (parse(gs.get.value).extract[OrganizationsResult].some).successNel[Throwable];
            }
          }
        }
    }
  }
}

