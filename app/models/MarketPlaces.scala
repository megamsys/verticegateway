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
package models

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
//import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps
import org.megam.util.Time
import controllers.stack._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import models._
import models.riak._
import models.cache._
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

case class MarketPlacePlan(price: String, description: String, plantype: String, version: String, source: String, os: String) {
  val json = "{\"price\":\"" + price +
    "\",\"description\":\"" + description + "\",\"plantype\":\"" + plantype +
    "\",\"version\":\"" + version + "\",\"source\":\"" + source + "\",\"os\":\"" + os + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.MarketPlacePlanSerialization.{ writer => MarketPlacePlanWriter }
    toJSON(this)(MarketPlacePlanWriter)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object MarketPlacePlan {
  def empty: MarketPlacePlan = new MarketPlacePlan(new String(), new String(), new String(), new String, new String(), new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[MarketPlacePlan] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.MarketPlacePlanSerialization.{ reader => MarketPlacePlanReader }
    fromJSON(jValue)(MarketPlacePlanReader)
  }

  def fromJson(json: String): Result[MarketPlacePlan] = (Validation.fromTryCatch[net.liftweb.json.JValue] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}


case class MarketPlaceCatalog(logo: String, category: String, description: String) {
  val json = "\"logo\":\"" + logo + "\",\"category\":\"" + category + "\",\"description\":\"" + description + "\""
}

case class MarketPlaceInput(name: String, catalog: MarketPlaceCatalog, plans: models.MarketPlacePlans, cattype: String, predef: String, status: String) {
  val json = "{\"name\":\"" + name + "\",\"catalog\":{" + catalog.json + "},\"plans\":" + MarketPlacePlans.toJson(plans, true) + ",\"cattype\":\"" + cattype + "\",\"predef\":\"" + predef + "\",\"status\":\"" + status + "\"}"
}


//init the default market place addons
object MarketPlaceInput {

  val ACTIVE  = "ACTIVE"

  val APP     = "APP"
  val SERVICE = "SERVICE"
  val DEW     = "DEW"


   val toMap = Map[String, MarketPlaceInput](
"1-Dew-Ubuntu" -> MarketPlaceInput("1-Ubuntu", new MarketPlaceCatalog("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/ubuntu.png", "Dew", "Ubuntu Server"), MarketPlacePlans(List(new MarketPlacePlan("0", "Ubuntu 12.04 LTS (Precise Pangolin) is the Ubuntu's sixteenth release and its fourth Long Term Support (LTS) release.", "Free", "12.04", "", "Ubuntu 12.04 +"), new MarketPlacePlan("0", "Shuttleworth indicated that the focus in this development cycle would be a release characterized by 'performance, refinement, maintainability, technical debt' and encouraged the developers to make conservative choices.", "Free", "14.04", "", "Ubuntu 14.04 +"))), DEW, "ubuntu", ACTIVE),
"2-Dew-CoreOS" -> MarketPlaceInput("2-CoreOS", new MarketPlaceCatalog("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/coreos.png", "Dew", "CoreOS"), MarketPlacePlans(List(new MarketPlacePlan("0", "CoreOS provides no package manager as a way for the distribution of applications, requiring instead all applications to run inside their containers.", "Free", "633.1.0", "", "CoreOS 633.1.0"))), DEW, "coreos", ACTIVE),
"3-Dew-Debian" -> MarketPlaceInput("3-Debian", new MarketPlaceCatalog("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/debian.png", "Dew", "Debian"), MarketPlacePlans(List(new MarketPlacePlan("0", "Debian wheezy.", "Free", "7", "", "Debian wheezy"), new MarketPlacePlan("0", "Debian Jessie.", "Free", "8", "", "Debian Jessie"))), DEW, "debian", ACTIVE),
"4-Dew-CentOS" -> MarketPlaceInput("4-CentOS", new MarketPlaceCatalog("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/centos.png", "Dew", "CentOS"), MarketPlacePlans(List(new MarketPlacePlan("0", "CentOS is an Enterprise-class Linux Distribution derived from sources freely provided to the public by Red Hat.", "Free", "7", "", "CentOS 7"))), DEW, "centos", ACTIVE),
"5-StarterPack-Java" -> MarketPlaceInput("5-Java", new MarketPlaceCatalog("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/java.png", "Starter packs", "Java Web Starter"), MarketPlacePlans(List(new MarketPlacePlan("0", "Quickly get started with J2EE Spring framework app and a light-weight database.", "Free", "0.5", "", "Ubuntu 14.04 +"))), APP, "java", ACTIVE),
"6-StarterPack-Rails" -> MarketPlaceInput("6-Rails", new MarketPlaceCatalog("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/rails.png", "Starter packs", "Rails App"), MarketPlacePlans(List(new MarketPlacePlan("0", "Quickly get started with rails app and a light-weight database.", "Free", "0.5", "", "Ubuntu 14.04 +"))), APP, "rails", ACTIVE),
"7-StarterPack-Play" -> MarketPlaceInput("7-Play", new MarketPlaceCatalog("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/play.png", "Starter packs", "RESTful API Server"), MarketPlacePlans(List(new MarketPlacePlan("0", "Build robust RESTful API server using NoSQL(Riak).", "Free", "0.5", "", "Ubuntu 14.04 +"))), APP, "play", ACTIVE),
"8-StarterPack-Nodejs" -> MarketPlaceInput("8-Nodejs", new MarketPlaceCatalog("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/nodejs.png", "Starter packs", "Realtime App"), MarketPlacePlans(List(new MarketPlacePlan("0", "Build fast, scalable, and incredibly efficient blogging platform with light weight database.", "Free", "0.5", "", "Ubuntu 14.04 +"))), APP, "nodejs", ACTIVE),
"9-Platform-Docker" -> MarketPlaceInput("9-Docker", new MarketPlaceCatalog("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/docker.png", "Platform", "Container"), MarketPlacePlans(List(new MarketPlacePlan("0", "Docker that automates the deployment of applications inside software containers.", "Free", "0.5", "", "Ubuntu 14.04 +"))), "false", "docker", ACTIVE),
"10-AppBoilers-PostgreSQL" -> MarketPlaceInput("10-PostgreSQL", new MarketPlaceCatalog("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/postgres.png", "App Boilers", "Object Relational DBMS"), MarketPlacePlans(List(new MarketPlacePlan("0", "PostgreSQL is a powerful, open source object-relational database system.", "Free", "9.3", "", "Ubuntu 14.04 +"))), SERVICE, "postgresql", ACTIVE),
"11-AppBoilers-Riak" -> MarketPlaceInput("11-Riak", new MarketPlaceCatalog("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/riak.png", "App Boilers", "Scalable Distributed Database"),
MarketPlacePlans(List(new MarketPlacePlan("0", "Riak is a distributed database designed to deliver maximum data availability by distributing data across multiple servers.", "Free", "2.1.0", "http://s3.amazonaws.com/downloads.basho.com/riak/2.0/2.0.5/ubuntu/trusty/riak_2.0.5-1_amd64.deb", "Ubuntu 14.04 +"))), SERVICE, "riak", ACTIVE),
"12-AppBoilers-Redis" -> MarketPlaceInput("12-Redis", new MarketPlaceCatalog("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/redis.png", "App Boilers", "Key Value Store"), MarketPlacePlans(List(new MarketPlacePlan("0", "Redis is a key-value store which acts as a data structure server with keys containing strings, hashes, lists, sets and sorted sets.", "Free", "2.8.4", "", "Ubuntu 14.04 +"))), SERVICE, "redis", ACTIVE),
"13-AppBoilers-RabbitMQ" -> MarketPlaceInput("13-RabbitMQ", new MarketPlaceCatalog("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/rabbitmq.png", "App Boilers", "Message Broker"), MarketPlacePlans(List(new MarketPlacePlan("0", "RabbitMQ is a message broker software  that implements the Advanced Message Queuing Protocol (AMQP).", "Free", "3.3.5", "", "Ubuntu 14.04 +"))), SERVICE, "rabbitmq", ACTIVE),
"14-Analytics-Hadoop" -> MarketPlaceInput("14-Hadoop", new MarketPlaceCatalog("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/hadoop.png", "Analytics", "Plumbing your big data is easy"), MarketPlacePlans(List(new MarketPlacePlan("0", "Apache Hadoop is a set of algorithms (an open-source software framework) for distributed storage and distributed processing of very large data sets (Big Data) on computer clusters built from commodity hardware", "Free", "2.6.0", "", "Ubuntu 14.04 +"))), SERVICE, "hadoop", ACTIVE)
)

  val toStream = toMap.keySet.toStream

}

//case class MarketPlaceResult(id: String, name: String, catalog: MarketPlaceCatalog, features: MarketPlaceFeatures, plans: MarketPlacePlans, applinks: MarketPlaceAppLinks, attach: String, predefnode: String, approved: String, created_at: String) {
case class MarketPlaceResult(id: String, name: String, catalog: MarketPlaceCatalog, plans: MarketPlacePlans, cattype: String, predef: String, status: String, created_at: String) {

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

  def fromJson(json: String): Result[MarketPlaceResult] = (Validation.fromTryCatch[net.liftweb.json.JValue] {
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
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaces", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val marketPlaceInput: ValidationNel[Throwable, MarketPlaceInput] = (Validation.fromTryCatch[models.MarketPlaceInput] {
      parse(input).extract[MarketPlaceInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      mkp <- marketPlaceInput
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "mkp").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(mkp.name)
      val json = new MarketPlaceResult(uir.get._1 + uir.get._2, mkp.name, mkp.catalog, mkp.plans, mkp.cattype, mkp.predef, mkp.status, Time.now.toString).toJson(false)
      new GunnySack(mkp.name, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  private def mkGunnySack_init(input: MarketPlaceInput): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug("models.MarketPlaces mkGunnySack_init: entry--------------------:\n" + input.json)
    val marketplaceInput: ValidationNel[Throwable, MarketPlaceInput] = (Validation.fromTryCatch[models.MarketPlaceInput] {
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
      val mkpJson = new MarketPlaceResult(uir.get._1 + uir.get._2, mkp.name, mkp.catalog, mkp.plans, mkp.cattype, mkp.predef, mkp.status, Time.now.toString).toJson(false)
      new GunnySack(mkp.name, mkpJson, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new market place item with the 'name' of the item provide as input.
   * A index name marketplace name will point to the "marketplaces" bucket.
   */
  def create(email: String, input: String): ValidationNel[Throwable, Option[MarketPlaceResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaces", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }). //riak storage
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

  def createMany(marketPlaceInput: Map[String, MarketPlaceInput]): ValidationNel[Throwable, MarketPlaceResults] = {
    play.api.Logger.debug("models.MarketPlaces create: entry")
    play.api.Logger.debug(("%-20s -->[%s]").format("value", marketPlaceInput))
    (marketPlaceInput.toMap.some map {
      _.map { p =>
        play.api.Logger.debug(("%-20s -->[%s]").format("value", p))
        (mkGunnySack_init(p._2) leftMap { t: NonEmptyList[Throwable] => t
        }).flatMap { gs: Option[GunnySack] =>
          (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
            flatMap { maybeGS: Option[GunnySack] =>
              maybeGS match {
                case Some(thatGS) => MarketPlaceResults(parse(thatGS.value).extract[MarketPlaceResult]).successNel[Throwable]
                case None => {
                  play.api.Logger.warn(("%-20s -->[%s]").format("MarketPlaces.created success", "Scaliak returned => None. Thats OK."))
                  MarketPlaceResults(MarketPlaceResult(new String(), p._2.name, p._2.catalog, p._2.plans, p._2.cattype, p._2.predef, p._2.status, new String())).successNel[Throwable]
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
        InMemory[ValidationNel[Throwable, MarketPlaceResults]]({
          cname: String =>
            {
              play.api.Logger.debug("models.MarketPlaceName findByName: marketplaces:" + marketplacesName)
              (riak.fetch(marketplacesName) leftMap { t: NonEmptyList[Throwable] =>
                new ServiceUnavailableError(marketplacesName, (t.list.map(m => m.getMessage)).mkString("\n"))
              }).toValidationNel.flatMap { xso: Option[GunnySack] =>
                xso match {
                  case Some(xs) => {
                    (Validation.fromTryCatch[models.MarketPlaceResult] {
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
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlace", "listAll:Entry"))
    findByName(MarketPlaceInput.toStream.some) //return the folded element in the head.
  }

  implicit val sedimentMarketPlacesResults = new Sedimenter[ValidationNel[Throwable, MarketPlaceResults]] {
    def sediment(maybeASediment: ValidationNel[Throwable, MarketPlaceResults]): Boolean = {
      val notSed = maybeASediment.isSuccess
      play.api.Logger.debug("%-20s -->[%s]".format("|^/^|-->MKP:sediment:", notSed))
      notSed
    }
  }

}
