/* 
** Copyright [2013-2014] [Megam Systems]
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
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps
import org.megam.util.Time
import controllers.stack._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import models._
import models.riak._
import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{RiakIndexes, StringBinIndex, LongIntIndex }
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

case class MarketPlaceAddonsInput(node_id: String, node_name: String, marketplace_id: String, config: MarketPlaceAddonsConfig) {
  val json = "{\"node_id\":\"" + node_id + "\",\"node_name\":\"" + node_name + "\",\"marketplace_id\":\"" + marketplace_id + "\",\"config\":" + config + "}"
}

case class MarketPlaceAddonsResult(id: String, node_id: String, node_name: String, marketplace_id: String, config_id: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.MarketPlaceAddonsResultSerialization
    val preser = new MarketPlaceAddonsResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object MarketPlaceAddonsResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[MarketPlaceAddonsResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.MarketPlaceAddonsResultSerialization
    val preser = new MarketPlaceAddonsResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[MarketPlaceAddonsResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue,Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object MarketPlaceAddons {

  implicit val formats = DefaultFormats
  private val riak = GWRiak( "addons")
  implicit def MarketPlaceAddonsResultsSemigroup: Semigroup[MarketPlaceAddonsResults] = Semigroup.instance((f1, f2) => f1.append(f2))

  val metadataKey = "marketplaceaddons"
  val metadataVal = "MarketPlaceAddons Creation"
  val bindex = "marketplaceaddons"

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaceAddons", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val addonInput: ValidationNel[Throwable, MarketPlaceAddonsInput] = (Validation.fromTryCatchThrowable[models.MarketPlaceAddonsInput, Throwable] {
      parse(input).extract[MarketPlaceAddonsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
    play.api.Logger.debug(("%-20s -->[%s]").format("json-------->", addonInput))
    for {
      pdc <- addonInput
      con <- (MarketPlaceAddonsConfiguration.create("{\"node_id\":\"" + pdc.node_id + "\",\"node_name\":\"" + pdc.node_name + "\",\"config\":" + pdc.config.json + "}") leftMap { t: NonEmptyList[Throwable] => t })
      //TO-DO: Does the leftMap make sense ? To check during function testing, by removing it.
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "aon").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for None on aor, and uir (confirm it during function testing).
      val bvalue = Set(pdc.node_id)
      val con_value = con.getOrElse(throw new ServiceUnavailableError(pdc.node_name, "Addon configuration create failed (or) not found. Retry again."))
      val json = new MarketPlaceAddonsResult(uir.get._1 + uir.get._2, pdc.node_id, pdc.node_name, pdc.marketplace_id, con_value.id, Time.now.toString).toJson(false)
      new GunnySack(uir.get._1 + uir.get._2, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new Node with the 'name' of the node provide as input.
   * A index name accountID will point to the "accounts" bucket
   */
  def create(email: String, input: String): ValidationNel[Throwable, Option[Tuple3[Map[String, String], String, String]]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaceAddons", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          val req_input = parse(input).extract[MarketPlaceAddonsInput]
          val req_result = parse(gs.get.value).extract[MarketPlaceAddonsResult]
          maybeGS match {
            case Some(thatGS) => {
              if (req_input.config.disaster.tohosts.equals("")) {
                Tuple3(Map[String, String](("Id" -> req_result.config_id), ("Action" -> "addon"), ("Args" -> "Nah")), req_result.node_name, null).some.successNel[Throwable]
              } else {
                Tuple3(Map[String, String](("Id" -> req_result.config_id), ("Action" -> "addon"), ("Args" -> "Nah")), req_result.node_name, req_input.config.disaster.tohosts).some.successNel[Throwable]
              }
            }
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("MarketPlaceAddons.created success", "Scaliak returned => None. Thats OK."))
              //(parse(gs.get.value).extract[MarketPlaceAddonsResult].some).successNel[Throwable];
              if (req_input.config.disaster.tohosts.equals("")) {
                Tuple3(Map[String, String](("Id" -> req_result.config_id), ("Action" -> "addon"), ("Args" -> "Nah")), req_result.node_name, null).some.successNel[Throwable]
              } else {
                Tuple3(Map[String, String](("Id" -> req_result.config_id), ("Action" -> "addon"), ("Args" -> "Nah")), req_result.node_name, req_input.config.disaster.tohosts).some.successNel[Throwable]
              }
            }
          }
        }
    }
  }

  /**
   * List all the app defns for a list of appdefns id for a particular node.
   */
  def findByAddonId(addonidList: Option[List[String]]): ValidationNel[Throwable, MarketPlaceAddonsResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaceAddons", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("nodeNameList", addonidList))
    (addonidList map {
      _.map { addonid =>
        play.api.Logger.debug(("%-20s -->[%s]").format("Add on id", addonid))
        (riak.fetch(addonid) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(addonid, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              //JsonScalaz.Error doesn't descend from java.lang.Error or Throwable. Screwy.
              (MarketPlaceAddonsResult.fromJson(xs.value) leftMap
                { t: NonEmptyList[net.liftweb.json.scalaz.JsonScalaz.Error] =>
                  JSONParsingError(t)
                }).toValidationNel.flatMap { j: MarketPlaceAddonsResult =>
                  play.api.Logger.debug(("%-20s -->[%s]").format("MarketPlaceAddonsresult", j))
                  Validation.success[Throwable, MarketPlaceAddonsResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ? 
                }
            }
            case None => {
              Validation.failure[Throwable, MarketPlaceAddonsResults](new ResourceItemNotFound(addonid, "")).toValidationNel
            }
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((MarketPlaceAddonsResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head. 
  }

  def findByNodeName(nodeNameList: Option[List[String]]): ValidationNel[Throwable, MarketPlaceAddonsResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaceAddons", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("nodeNameList", nodeNameList))
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Throwable, MarketPlaceAddonsResults]] {
      ((((for {
        nelnr <- (Nodes.findByNodeName(nodeNameList) leftMap { t: NonEmptyList[Throwable] => t })
      } yield {
        //this is ugly, since what we receive from Nodes always contains one None. We need to filter
        //that. This is justa  hack for now. It calls for much more elegant soln.
        (nelnr.list filter (nelwop => nelwop.isDefined) map { nelnor: Option[NodeResult] =>
          (if (nelnor.isDefined) { //we only want to use the Some, ignore None. Hence a pattern match wasn't used here.
            val bindex = ""
            val bvalue = Set("")
            val metadataVal = "Nodes-name"
            play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaceAddons", nelnor))
            new GunnySack("marketplaceaddons", nelnor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
              None, Map(metadataKey -> metadataVal), Map((bindex, bvalue)))
          }).asInstanceOf[GunnySack]
        })
      }) leftMap { t: NonEmptyList[Throwable] => t } flatMap (({ gs: List[GunnySack] =>
        gs.map { ngso: GunnySack => riak.fetchIndexByValue(ngso) }
      }) map {
        _.foldRight((List[String]()).successNel[Throwable])(_ +++ _)
      })) map { nm: List[String] =>
        play.api.Logger.debug("------------->" + nm)
        (if (!nm.isEmpty) findByAddonId(nm.some) else
          new ResourceItemNotFound(nodeNameList.map(m => m.mkString("[", ",", "]")).get, "application MarketPlaceAddons = nothing found.").failureNel[MarketPlaceAddonsResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    res.getOrElse(new ResourceItemNotFound(nodeNameList.map(m => m.mkString("[", ",", "]")).get, "application MarketPlaceAddons = nothing found.").failureNel[MarketPlaceAddonsResults])

  }

  /*
   * An IO wrapped finder using an email. Upon fetching the node results for an email, 
   * the nodeids are listed in bucket `Requests`.
   * Using a "requestid" as key, return a list of ValidationNel[List[RequestResult]] 
   * Takes an email, and returns a Future[ValidationNel, List[Option[RequestResult]]]
   */
  /*def findByEmail(email: String): ValidationNel[Throwable, MarketPlaceAddonsResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaceAddons", "findByEmail:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Throwable, MarketPlaceAddonsResults]] {
      ((((for {
        nelnr <- (Nodes.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      } yield {
        play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaceAddons", "fetched nodes by email"))
        //this is ugly, since what we receive from Nodes always contains one None. We need to filter
        //that. This is justa  hack for now. It calls for much more elegant soln.
        (nelnr.list filter (nelwop => nelwop.isDefined) map { nelnor: Option[NodeResult] =>
          val bindex = ""
          val bvalue = Set("")
          val metadataVal = "Nodes-name"
          play.api.Logger.debug(("%-20s -->[%s]").format("models.Definition", nelnor))
          new GunnySack("nodeId", nelnor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
            None, Map(metadataKey -> metadataVal), Map((bindex, bvalue)))
        })
      }) leftMap { t: NonEmptyList[Throwable] => t } flatMap (({ gs: List[GunnySack] =>
        gs.map { ngso: GunnySack => riak.fetchIndexByValue(ngso) }
      }) map {
        _.foldLeft((List[String]()).successNel[Throwable])(_ +++ _)
      })) map { nm: List[String] =>
        (if (!nm.isEmpty) findByAddonId(nm.some) else
          new ResourceItemNotFound(email, "definitions = nothing found.").failureNel[MarketPlaceAddonsResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaceAddons", res))
    res.getOrElse(new ResourceItemNotFound(email, "definitions = nothing found.").failureNel[MarketPlaceAddonsResults])
  }*/

}