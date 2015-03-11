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

case class MarketPlaceAddonsConfig(disaster: MarketPlaceAddonsConfigDisaster, loadbalancing: MarketPlaceAddonsConfigLoadBalancing, autoscaling: MarketPlaceAddonsConfigAutoScaling, monitoring: MarketPlaceAddonsConfigMonitoring) {
   val json = "{\"disaster\":" + disaster.json + ",\"loadbalancing\":" + loadbalancing.json + ",\"autoscaling\":" + autoscaling.json + ",\"monitoring\":" + monitoring.json + "}"
}

case class MarketPlaceAddonsConfigDisaster(locations: String, fromhost: String, tohosts: String, recipe: String) {
  val json =  "{\"locations\":\"" + locations + "\",\"fromhost\":\"" + fromhost + "\",\"tohosts\":\"" + tohosts + "\",\"recipe\":\"" + recipe + "\"}"
}

case class MarketPlaceAddonsConfigLoadBalancing(haproxyhost: String, loadbalancehost: String, recipe: String) {
  val json =  "{\"haproxyhost\":\"" + haproxyhost + "\",\"loadbalancehost\":\"" + loadbalancehost + "\",\"recipe\":\"" + recipe + "\"}"
}

case class MarketPlaceAddonsConfigAutoScaling(cputhreshold: String, memorythreshold: String, noofinstances: String, recipe: String) {
  val json =  "{\"cputhreshold\":\"" + cputhreshold + "\",\"memorythreshold\":\"" + memorythreshold + "\",\"noofinstances\":\"" + noofinstances + "\",\"recipe\":\"" + recipe + "\"}"
}

case class MarketPlaceAddonsConfigMonitoring(agent: String, recipe: String) {
  val json =  "{\"agent\":\"" + agent + "\",\"recipe\":\"" + recipe + "\"}"
}

case class MarketPlaceAddonsConfigurationInput(node_id: String, node_name: String, config: MarketPlaceAddonsConfig) {
  val json = "{\"node_id\":\"" + node_id + "\",\"node_name\":\"" + node_name + "\",\"config\":\"" + config.json + "\"}"
}

case class MarketPlaceAddonsConfigurationResult(id: String, node_id: String, node_name: String, config: MarketPlaceAddonsConfig, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.MarketPlaceAddonsConfigurationResultSerialization
    val preser = new MarketPlaceAddonsConfigurationResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object MarketPlaceAddonsConfigurationResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[MarketPlaceAddonsConfigurationResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.MarketPlaceAddonsConfigurationResultSerialization
    val preser = new MarketPlaceAddonsConfigurationResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[MarketPlaceAddonsConfigurationResult] = (Validation.fromTryCatch[net.liftweb.json.JValue] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object MarketPlaceAddonsConfiguration {

  implicit val formats = DefaultFormats
  private val riak = GWRiak( "addonconfigs")
  implicit def MarketPlaceAddonsConfigurationResultsSemigroup: Semigroup[MarketPlaceAddonsConfigurationResults] = Semigroup.instance((f1, f2) => f1.append(f2))

  val metadataKey = "marketplaceaddonsconfig"
  val metadataVal = "MarketPlaceAddonsconfig Creation"
  val bindex = "nodesId"

 
  private def mkGunnySack(input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaceAddonsConfiguration", "mkGunnySack:Entry")) 
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val configInput: ValidationNel[Throwable, MarketPlaceAddonsConfigurationInput] = (Validation.fromTryCatch[MarketPlaceAddonsConfigurationInput] {
      parse(input).extract[MarketPlaceAddonsConfigurationInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      pdc <- configInput
      //TO-DO: Does the leftMap make sense ? To check during function testing, by removing it.
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "con").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for None on aor, and uir (confirm it during function testing).
      val bvalue = Set(pdc.node_id)
      val json = new MarketPlaceAddonsConfigurationResult(uir.get._1 + uir.get._2, pdc.node_id, pdc.node_name, pdc.config, Time.now.toString).toJson(false)
      new GunnySack(uir.get._1 + uir.get._2, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new Node with the 'name' of the node provide as input.
   * A index name accountID will point to the "accounts" bucket
   */
  def create(input: String): ValidationNel[Throwable, Option[MarketPlaceAddonsConfigurationResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaceAddons", "create:Entry"))   
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[MarketPlaceAddonsConfigurationResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("MarketPlaceAddonsConfiguration.created success", "Scaliak returned => None. Thats OK."))
              (parse(gs.get.value).extract[MarketPlaceAddonsConfigurationResult].some).successNel[Throwable];
            }
          }

        }
    }
  }

  /**
   * List all the app defns for a list of appdefns id for a particular node.
   */
  def findByAddonConfigId(addonconfigidList: Option[List[String]]): ValidationNel[Throwable, MarketPlaceAddonsConfigurationResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaceAddonsConfiguration", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("nodeNameList", addonconfigidList))
    (addonconfigidList map {
      _.map { addonconfigid =>
        play.api.Logger.debug(("%-20s -->[%s]").format("Add on id", addonconfigid))
        (riak.fetch(addonconfigid) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(addonconfigid, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              //JsonScalaz.Error doesn't descend from java.lang.Error or Throwable. Screwy.
              (MarketPlaceAddonsConfigurationResult.fromJson(xs.value) leftMap
                { t: NonEmptyList[net.liftweb.json.scalaz.JsonScalaz.Error] =>
                  JSONParsingError(t)
                }).toValidationNel.flatMap { j: MarketPlaceAddonsConfigurationResult =>
                  play.api.Logger.debug(("%-20s -->[%s]").format("MarketPlaceAddonsConfigurationresult", j))
                  Validation.success[Throwable, MarketPlaceAddonsConfigurationResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ? 
                }
            }
            case None => {
              Validation.failure[Throwable, MarketPlaceAddonsConfigurationResults](new ResourceItemNotFound(addonconfigid, "")).toValidationNel
            }
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((MarketPlaceAddonsConfigurationResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head. 
  }

  /*def findByNodeName(nodeNameList: Option[List[String]]): ValidationNel[Throwable, MarketPlaceAddonsConfigurationResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaceAddonsConfiguration", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("nodeNameList", nodeNameList))
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Throwable, MarketPlaceAddonsConfigurationResults]] {
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
            play.api.Logger.debug(("%-20s -->[%s]").format("models.MarketPlaceAddonsConfiguration", nelnor))
            new GunnySack("nodesId", nelnor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
              None, Map(metadataKey -> metadataVal), Map((bindex, bvalue)))
          }).asInstanceOf[GunnySack]
        })
      }) leftMap { t: NonEmptyList[Throwable] => t } flatMap (({ gs: List[GunnySack] =>
        gs.map { ngso: GunnySack => riak.fetchIndexByValue(ngso) }
      }) map {
        _.foldRight((List[String]()).successNel[Throwable])(_ +++ _)
      })) map { nm: List[String] =>
        play.api.Logger.debug("------------->" + nm)
        (if (!nm.isEmpty) findByAddonConfigId(nm.some) else
          new ResourceItemNotFound(nodeNameList.map(m => m.mkString("[", ",", "]")).get, "application MarketPlaceAddonsConfiguration = nothing found.").failureNel[MarketPlaceAddonsConfigurationResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    res.getOrElse(new ResourceItemNotFound(nodeNameList.map(m => m.mkString("[", ",", "]")).get, "application MarketPlaceAddonsConfiguration = nothing found.").failureNel[MarketPlaceAddonsConfigurationResults])

  }
 */
}