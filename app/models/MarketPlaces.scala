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
package models

import scalaz._
import scalaz.syntax.SemigroupOps
import scalaz.NonEmptyList._
import scalaz.Validation._
import scalaz.effect.IO
import scalaz.EitherT._
import com.twitter.util.Time
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
 * @author rajthilak
 *
 */
/* name, logo, catagory, pricetype, attach, approved, feature-1,2,3,4, plan->{price, description, type}, predefnode */

case class MarketPlacePlan(price: String, description: String, plantype: String) {
  val json = "\"price\":\"" + price + "\",\"description\":\"" + description + "\",\"plantype\":\"" + plantype + "\""
}

case class MarketPlaceFeatures(feature1: String, feature2: String, feature3: String, feature4: String) {
  val json = "\"feature1\":\"" + feature1 + "\",\"feature2\":\"" + feature2 + "\",\"feature3\":\"" + feature3 + "\",\"feature4\":\"" + feature4 + "\""
}

case class MarketPlaceInput(name: String, logo: String, catagory: String, pricetype: String, features: MarketPlaceFeatures, plan: MarketPlacePlan, attach: String, predefnode: String, approved: String) {
  val json = "{\"name\":\"" + name + "\",\"logo\":\"" + logo + "\",\"catagory\":\"" + catagory + "\",\"pricetype\":\"" + pricetype + "\",\"features\":{" + features.json + "},\"plan\":{" + plan.json + "},\"attach\":\"" + attach + "\",\"predefnode\":\"" + predefnode + "\",\"approved\":\"" + approved + "\"}"
}

//init the default market place addons
object MarketPlaceInput {

  val toMap = Map[String, MarketPlaceInput](
    "alfresco" -> MarketPlaceInput("alfresco", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/alfresco.png", "ECM", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "diaspora" -> MarketPlaceInput("diaspora", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/diaspora.png", "Social", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "dokuwiki" -> MarketPlaceInput("dokuwiki", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/dokuwiki.png", "Wiki", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "drbd" -> MarketPlaceInput("drbd", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/drbd.png", "DR", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "dreamfactory" -> MarketPlaceInput("dreamfactory", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/dreamfactory.png", "Mobile Development", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "drupal" -> MarketPlaceInput("drupal", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/drupal.png", "CMS", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "elgg" -> MarketPlaceInput("elgg", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/elgg.png", "Social", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "firepad" -> MarketPlaceInput("firepad", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/firepad.png", "Cloud Editor", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "ghost" -> MarketPlaceInput("ghost", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/ghost.png", "Blog", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "gitlab" -> MarketPlaceInput("gitlab", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/gitlab.png", "Continuous Integration", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "hadoop" -> MarketPlaceInput("hadoop", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/hadoop.png", "Business Intelligence", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "jenkins" -> MarketPlaceInput("jenkins", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/jenkins.png", "Continuous Integration", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "joomla" -> MarketPlaceInput("joomla", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/joomla.png", "CMS", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "liferay" -> MarketPlaceInput("liferay", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/liferay.png", "Collaboration", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "magneto" -> MarketPlaceInput("magneto", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/magneto.png", "e-Commerce", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "mediawiki" -> MarketPlaceInput("mediawiki", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/mediawiki.png", "Wiki", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "openam" -> MarketPlaceInput("openam", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/openam.png", "AM", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "openatrium" -> MarketPlaceInput("openatrium", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/openatrium.png", "Project Management", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "opendj" -> MarketPlaceInput("opendj", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/opendj.png", "AM", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "openerp" -> MarketPlaceInput("openerp", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/openerp.png", "ERP", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "openldap" -> MarketPlaceInput("openldap", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/openldap.png", "Directory Services", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "otrs" -> MarketPlaceInput("otrs", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/otrs.png", "HelpDesk", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "owncloud" -> MarketPlaceInput("owncloud", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/owncloud.png", "Media sharing", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "redmine" -> MarketPlaceInput("redmine", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/redmine.png", "Collaboration", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "reviewboard" -> MarketPlaceInput("reviewboard", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/reviewboard.png", "Collaboration", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "riak" -> MarketPlaceInput("riak", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/riak.png", "DB", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "scmmanager" -> MarketPlaceInput("scmmanager", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/scmmanager.png", "Development Platform", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "sugarcrm" -> MarketPlaceInput("sugarcrm", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/sugarcrm.png", "CRM", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "thinkup" -> MarketPlaceInput("thinkup", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/thinkup.png", "Social", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "trac" -> MarketPlaceInput("trac", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/trac.png", "Bug Tracking", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "twiki" -> MarketPlaceInput("twiki", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/twiki.png", "Wiki", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "wordpress" -> MarketPlaceInput("wordpress", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/wordpress.png", "CMS", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "xwiki" -> MarketPlaceInput("xwiki", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/xwiki.png", "Wiki", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved"),
    "zarafa" -> MarketPlaceInput("zarafa", "https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/zarafa.png", "Firewall", "free", new MarketPlaceFeatures("feature1", "feature2", "feature3", "feature4"), new MarketPlacePlan("50", "description", "free"), "attach", "predefnode", "approved")
  )

  val toStream = toMap.keySet.toStream

}

case class MarketPlaceResult(id: String, name: String, logo: String, catagory: String, pricetype: String, features: MarketPlaceFeatures, plan: MarketPlacePlan, attach: String, predefnode: String, approved: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.MarketPlaceResultSerialization
    val preser = new MarketPlaceResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object MarketPlaceResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[MarketPlaceResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.MarketPlaceResultSerialization
    val preser = new MarketPlaceResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[MarketPlaceResult] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object MarketPlaces {

  implicit val formats = DefaultFormats
  private def riak: GSRiak = GSRiak(MConfig.riakurl, "marketplaces")
  implicit def MarketPlacesSemigroup: Semigroup[MarketPlaceResults] = Semigroup.instance((f1, f2) => f1.append(f2))

  val metadataKey = "MarketPlace"
  val metadataVal = "MarketPlaces Creation"
  val bindex = BinIndex.named("marketplace")

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaces", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val marketPlaceInput: ValidationNel[Throwable, MarketPlaceInput] = (Validation.fromTryCatch {
      parse(input).extract[MarketPlaceInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      mkp <- marketPlaceInput
      //TO-DO: Does the leftMap make sense ? To check during function testing, by removing it.
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "mkp").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for None on aor, and uir (confirm it during function testing).
      val bvalue = Set(mkp.name)
      val json = new MarketPlaceResult(uir.get._1 + uir.get._2, mkp.name, mkp.logo, mkp.catagory, mkp.pricetype, mkp.features, mkp.plan, mkp.attach, mkp.predefnode, mkp.approved, Time.now.toString).toJson(false)
      new GunnySack(mkp.name, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  private def mkGunnySack_init(input: MarketPlaceInput): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug("models.MarketPlaces mkGunnySack: entry:\n" + input.json)
    val marketplaceInput: ValidationNel[Throwable, MarketPlaceInput] = (Validation.fromTryCatch {
      parse(input.json).extract[MarketPlaceInput]
    } leftMap { t: Throwable => new MalformedBodyError(input.json, t.getMessage) }).toValidationNel //capture failure

    for {
      mkp <- marketplaceInput
      //TO-DO: Does the leftMap make sense ? To check during function testing, by removing it.
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "mkp").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for uir to filter the None case. confirm it during function testing.
      play.api.Logger.debug("models.marketplaces mkGunnySack: yield:\n" + (uir.get._1 + uir.get._2))
      val bvalue = Set(mkp.name)
      val mkpJson = new MarketPlaceResult(uir.get._1 + uir.get._2, mkp.name, mkp.logo, mkp.catagory, mkp.pricetype, mkp.features, mkp.plan, mkp.attach, mkp.predefnode, mkp.approved, Time.now.toString).toJson(false)
      new GunnySack(mkp.name, mkpJson, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new market place item with the 'name' of the item provide as input.
   * A index name marketplace name will point to the "marketplaces" bucket
   */
  def create(email: String, input: String): ValidationNel[Throwable, Option[MarketPlaceResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaces", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[MarketPlaceResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("MarketPlace.created success", "Scaliak returned => None. Thats OK."))
              (parse(gs.get.value).extract[MarketPlaceResult].some).successNel[Throwable];
            }
          }
        }
    }
  }

  def marketplace_init: ValidationNel[Throwable, MarketPlaceResults] = {
    play.api.Logger.debug("models.MarketPlaces create: entry")
    (MarketPlaceInput.toMap.some map {
      _.map { p =>
        (mkGunnySack_init(p._2) leftMap { t: NonEmptyList[Throwable] => t
        }).flatMap { gs: Option[GunnySack] =>
          (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
            flatMap { maybeGS: Option[GunnySack] =>
              maybeGS match {
                case Some(thatGS) => MarketPlaceResults(parse(thatGS.value).extract[MarketPlaceResult]).successNel[Throwable]
                case None => {
                  play.api.Logger.warn(("%-20s -->[%s]").format("MarketPlaces.created success", "Scaliak returned => None. Thats OK."))
                  MarketPlaceResults(MarketPlaceResult(new String(), p._2.name, p._2.logo, p._2.catagory, p._2.pricetype, p._2.features, p._2.plan, p._2.attach, p._2.predefnode, p._2.approved, new String())).successNel[Throwable]
                }
              }
            }
        }
      }
    } map {
      _.fold((MarketPlaceResults.empty).successNel[Throwable])(_ +++ _) //fold or foldRight ? 
    }).head //return the folded element in the head.  

  }

  def findByName(marketPlacesNameList: Option[Stream[String]]): ValidationNel[Throwable, MarketPlaceResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaces", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("marketPlaceList", marketPlacesNameList))
    (marketPlacesNameList map {
      _.map { marketplacesName =>
        play.api.Logger.debug("models.MarketPlaceName findByName: marketplaces:" + marketplacesName)
        (riak.fetch(marketplacesName) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(marketplacesName, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              (Validation.fromTryCatch {
                parse(xs.value).extract[MarketPlaceResult]
              } leftMap { t: Throwable =>
                new ResourceItemNotFound(marketplacesName, t.getMessage)
              }).toValidationNel.flatMap { j: MarketPlaceResult =>
                Validation.success[Throwable, MarketPlaceResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ? 
              }
            }
            case None => Validation.failure[Throwable, MarketPlaceResults](new ResourceItemNotFound(marketplacesName, "")).toValidationNel
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((MarketPlaceResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head. 

  }

   def listAll: ValidationNel[Throwable, MarketPlaceResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlace", "listAll:Entry"))
    findByName(MarketPlaceInput.toStream.some) //return the folded element in the head.  
  }

   implicit val sedimentPredefResults = new Sedimenter[ValidationNel[Error, MarketPlaceResults]] {
    def sediment(maybeASediment: ValidationNel[Error, MarketPlaceResults]): Boolean = {
      val notSed = maybeASediment.isSuccess
      play.api.Logger.debug("%-20s -->[%s]".format("|^/^|-->MKP:sediment:", notSed))
      notSed
    }
   }
   
}