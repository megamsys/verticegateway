/* 
** Copyright [2012-2013] [Megam Systems]
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
import com.stackmob.scaliak._
import com.basho.riak.client.query.indexes.{ RiakIndexes, IntIndex, BinIndex }
import com.basho.riak.client.http.util.{ Constants => RiakConstants }
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author morpheyesh
 *
 */

case class OrganizationsResult(id: String, name: String, appdetails: MarketPlaceAppDetails, features: MarketPlaceFeatures, plans: MarketPlacePlans, applinks: MarketPlaceAppLinks, attach: String, predefnode: String, approved: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.MarketPlaceResultSerialization
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
  private def riak: GSRiak = GSRiak(MConfig.riakurl, "organizations")
  
  val metadataKey = "Organizations"
  val metadataVal = "Organizations Creation"
  val bindex = BinIndex.named("organization")
  
   /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  
  
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Organizations", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val organizationsInput: ValidationNel[Throwable, OrganizationsInput] = (Validation.fromTryCatch {
      parse(input).extract[OrganizationsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      mkp <- organizationsInput
      //TO-DO: Does the leftMap make sense ? To check during function testing, by removing it.
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "mkp").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for None on aor, and uir (confirm it during function testing).
      val bvalue = Set(mkp.name)
      val json = new OrganizationsResult(uir.get._1 + uir.get._2, mkp.name, mkp.appdetails, mkp.features, mkp.plans, mkp.applinks, mkp.attach, mkp.predefnode, mkp.approved, Time.now.toString).toJson(false)
      new GunnySack(mkp.name, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }
  private def mkGunnySack_init(input: MarketPlaceInput): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug("models.MarketPlaces mkGunnySack_init: entry--------------------:\n" + input.json)
    val marketplaceInput: ValidationNel[Throwable, MarketPlaceInput] = (Validation.fromTryCatch {
      parse(input.json).extract[MarketPlaceInput]
    } leftMap { t: Throwable => new MalformedBodyError(input.json, t.getMessage) }).toValidationNel //capture failure
    play.api.Logger.debug("models.MarketPlaces mkGunnySack: entry--------------------:\n" + marketplaceInput)

    for {
      mkp <- marketplaceInput
      //TO-DO: Does the leftMap make sense ? To check during function testing, by removing it.
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "mkp").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for uir to filter the None case. confirm it during function testing.
      play.api.Logger.debug("models.marketplaces mkGunnySack: yield:\n" + (uir.get._1 + uir.get._2))
      val bvalue = Set(mkp.name)
      val mkpJson = new MarketPlaceResult(uir.get._1 + uir.get._2, mkp.name, mkp.appdetails, mkp.features, mkp.plans, mkp.applinks, mkp.attach, mkp.predefnode, mkp.approved, Time.now.toString).toJson(false)
      new GunnySack(mkp.name, mkpJson, RiakConstants.CTYPE_TEXT_UTF8, None,
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

