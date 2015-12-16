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
package models.base

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import models.json._
import models.base._
import db._
import cache._
import app.MConfig
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

case class MarketPlacePlan(description: String, version: String) {
  val json = "{\"description\":\"" + description + "\",\"version\":\"" + version + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.MarketPlacePlanSerialization.{ writer => MarketPlacePlanWriter }
    toJSON(this)(MarketPlacePlanWriter)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object MarketPlacePlan {
  def empty: MarketPlacePlan = new MarketPlacePlan(new String(), new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[MarketPlacePlan] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import MarketPlacePlanSerialization.{ reader => MarketPlacePlanReader }
    fromJSON(jValue)(MarketPlacePlanReader)
  }

  def fromJson(json: String): Result[MarketPlacePlan] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}



case class MarketPlaceInput(name: String, cattype: String, order: String, image: String, url: String, envs: models.tosca.KeyValueList, plans: models.base.MarketPlacePlans) {
  val json = "{\"name\":\"" + name + "\",\"cattype\":\"" + cattype + "\",\"order\":\"" + order + "\",\"image\":\"" + image + "\",\"url\":\"" + url + "\",\"envs\":" + models.tosca.KeyValueList.toJson(envs, true) + ",\"plans\":" + MarketPlacePlans.toJson(plans, true) + "}"
}

//init the default market place addons
object MarketPlaceInput {

  val toMap = utils.MKPData.mkMap

  val toStream = toMap.keySet.toStream

}

//case class MarketPlaceResult(id: String, name: String, catalog: MarketPlaceCatalog, features: MarketPlaceFeatures, plans: MarketPlacePlans, applinks: MarketPlaceAppLinks, attach: String, predefnode: String, approved: String, created_at: String) {
case class MarketPlaceResult(id: String, name: String, cattype: String, order: String, image: String, url: String, envs: models.tosca.KeyValueList, plans: MarketPlacePlans, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.MarketPlaceResultSerialization
    val preser = new MarketPlaceResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
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

  def fromJson(json: String): Result[MarketPlaceResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}


object MarketPlaces {

  implicit val formats = DefaultFormats

  private val riak = GWRiak("marketplaces")
  implicit def MarketPlacesSemigroup: Semigroup[MarketPlaceResults] = Semigroup.instance((f1, f2) => f1.append(f2))

  val metadataKey = "MarketPlace"
  val metadataVal = "MarketPlaces Creation"
  val bindex = "marketplace"


  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to marketplaceinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    val mktPlaceInput: ValidationNel[Throwable, MarketPlaceInput] = (Validation.fromTryCatchThrowable[models.base.MarketPlaceInput, Throwable] {
      parse(input).extract[MarketPlaceInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      mkp <- mktPlaceInput
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "mkp").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(mkp.name)
      val json = new MarketPlaceResult(uir.get._1 + uir.get._2, mkp.name, mkp.cattype, mkp.order, mkp.image, mkp.url, mkp.envs, mkp.plans, Time.now.toString).toJson(false)
      new GunnySack(mkp.name, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  private def mkGunnySack_init(input: MarketPlaceInput): ValidationNel[Throwable, Option[GunnySack]] = {
    val marketplaceInput: ValidationNel[Throwable, MarketPlaceInput] = (Validation.fromTryCatchThrowable[models.base.MarketPlaceInput, Throwable] {
      parse(input.json).extract[MarketPlaceInput]
    } leftMap { t: Throwable => new MalformedBodyError(input.json, t.getMessage) }).toValidationNel //capture failure

    for {
      mkp <- marketplaceInput
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "mkp").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(mkp.name)
      val mkpJson = new MarketPlaceResult(uir.get._1 + uir.get._2, mkp.name, mkp.cattype, mkp.order, mkp.image, mkp.url, mkp.envs, mkp.plans, Time.now.toString).toJson(false)
      new GunnySack(mkp.name, mkpJson, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new market place item with the 'name' of the item provide as input.
   * A index name marketplace name will point to the "marketplaces" bucket.
   */
  def create(email: String, input: String): ValidationNel[Throwable, Option[MarketPlaceResult]] = {
    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }). //riak storage
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[MarketPlaceResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD,"MarketPlace.created success",Console.RESET))
              (parse(gs.get.value).extract[MarketPlaceResult].some).successNel[Throwable];
            }
          }
        }
    }
  }

  def createMany(mktPlaceInput: Map[String, MarketPlaceInput]): ValidationNel[Throwable, MarketPlaceResults] = {
    (mktPlaceInput.toMap.some map {
      _.map { p =>
        (mkGunnySack_init(p._2) leftMap { t: NonEmptyList[Throwable] => t
        }).flatMap { gs: Option[GunnySack] =>
          (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
            flatMap { maybeGS: Option[GunnySack] =>
              maybeGS match {
                case Some(thatGS) => MarketPlaceResults(parse(thatGS.value).extract[MarketPlaceResult]).successNel[Throwable]
                case None => {
                  play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD,"MarketPlaces.created success", Console.RESET))
                  MarketPlaceResults(MarketPlaceResult(new String(), p._2.name, p._2.cattype, p._2.order, p._2.image, p._2.url, p._2.envs, p._2.plans, new String())).successNel[Throwable]
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
    (marketPlacesNameList map {
      _.map { marketplacesName =>
        InMemory[ValidationNel[Throwable, MarketPlaceResults]]({
          cname: String =>
            {
              play.api.Logger.debug("models.base.MarketPlaceName findByName: marketplaces:" + marketplacesName)
              (riak.fetch(marketplacesName) leftMap { t: NonEmptyList[Throwable] =>
                new ServiceUnavailableError(marketplacesName, (t.list.map(m => m.getMessage)).mkString("\n"))
              }).toValidationNel.flatMap { xso: Option[GunnySack] =>
                xso match {
                  case Some(xs) => {
                    (Validation.fromTryCatchThrowable[models.base.MarketPlaceResult, Throwable] {
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
            }
        }).get(marketplacesName).eval(InMemoryCache[ValidationNel[Throwable, MarketPlaceResults]]())
      }
    } map {
      _.foldRight((MarketPlaceResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head.

  }

  def listAll: ValidationNel[Throwable, MarketPlaceResults] = {
    findByName(MarketPlaceInput.toStream.some) //return the folded element in the head.
  }

  implicit val sedimentMarketPlacesResults = new Sedimenter[ValidationNel[Throwable, MarketPlaceResults]] {
    def sediment(maybeASediment: ValidationNel[Throwable, MarketPlaceResults]): Boolean = {
      val notSed = maybeASediment.isSuccess
      notSed
    }
  }

}
