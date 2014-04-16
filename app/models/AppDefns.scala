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

import scala.collection.immutable.Map
import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.syntax.SemigroupOps
import scalaz.NonEmptyList._
import scalaz.EitherT._
import scalaz.Validation._
import com.twitter.util.Time
import net.liftweb.json._
import java.nio.charset.Charset
import com.stackmob.scaliak._
import com.basho.riak.client.query.indexes.{ RiakIndexes, IntIndex, BinIndex }
import com.basho.riak.client.http.util.{ Constants => RiakConstants }
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import models.cache._
import controllers.funnel.FunnelErrors._
import controllers.Constants._
import net.liftweb.json.scalaz.JsonScalaz.{ Result, UncategorizedError }
import controllers.stack.MConfig
/**
 * @author rajthilak
 * authority
 */

case class AppDefnsResult(id: String, node_id: String, node_name: String, appdefns: NodeAppDefns, created_at: String) {
  val json = "{\"id\": \"" + id + "\",\"node_id\":\"" + node_id + "\",\"node_name\":\"" + node_name + "\",\"appdefns\":" + appdefns.json + ",\"created_at\":\"" + created_at + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.AppDefnsResultSerialization
    val acctser = new AppDefnsResultSerialization()
    toJSON(this)(acctser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }

}

object AppDefnsResult {

  def apply = new AppDefnsResult(new String(), new String(), new String(), new NodeAppDefns(new String(), new String(), new String(), new String(), new String()), new String())

  //def apply(timetokill: String): AppDefnsResult = AppDefnsResult(timetokill, new String(), new String(), new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[AppDefnsResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.AppDefnsResultSerialization
    val acctser = new AppDefnsResultSerialization()
    fromJSON(jValue)(acctser.reader)
  }

  def fromJson(json: String): Result[AppDefnsResult] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

case class AppDefnsInputforNewNode(node_id: String, node_name: String, appdefns: NodeAppDefns) {
  play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns:json", appdefns.json))
  val json = "\",\"node_id\":\"" + node_id + "\",\"node_name\":\"" + node_name + "\",\"appdefns\":" + appdefns.json
}

case class AppDefnsInputforExistNode(node_name: String, appdefns: NodeAppDefns) {
  play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns:json", appdefns.json))
  val json = "\",\"node_name\":\"" + node_name + "\",\"appdefns\":" + appdefns.json
}

case class AppDefnsUpdateInput(appdefn_id: String, node_name: String, runtime_exec: String, env_sh: String) {
  val json = "{\"appdefn_id\": \"" + appdefn_id + "\",\"node_name\": \"" + node_name + "\",\"runtime_exec\":\"" + runtime_exec + "\",\"env_sh\":\"" + env_sh + "\"}"
}

object AppDefns {

  implicit val formats = DefaultFormats

  private def riak: GSRiak = GSRiak(MConfig.riakurl, "appdefns")

  val metadataKey = "AppDefns"
  val newnode_metadataVal = "App Definition Creation"
  val newnode_bindex = BinIndex.named("appdefnsId")

  /**
   * A private method which chains computation to make GunnySack for existing node when provided with an input json, Option[node_name].
   * parses the json, and converts it to requestinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the GunnySack object is built.
   * If the node_name is send by the Node model. It then yield the GunnySack object.
   */
  private def mkGunnySackforExistNode(input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns:json", input))

    //Does this failure get propagated ? I mean, the input json parse fails ? I don't think so.
    //This is a potential bug.
    val ripNel: ValidationNel[Throwable, AppDefnsInputforExistNode] = (Validation.fromTryCatch {
      parse(input).extract[AppDefnsInputforExistNode]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns:rip", ripNel))
    for {
      rip <- ripNel
      aor <- (models.Nodes.findByNodeName(List(rip.node_name).some) leftMap { t: NonEmptyList[Throwable] => t })
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "adf").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val node_id = aor.list filter (nelwop => nelwop.isDefined) map { nelnor: Option[NodeResult] =>
        (if (nelnor.isDefined) { //we only want to use the Some, ignore None. Hence a pattern match wasn't used here.
          nelnor.get.id
        }).asInstanceOf[String]
      }
      val bvalue = Set(node_id(0))
      val json = "{\"id\": \"" + (uir.get._1 + uir.get._2) + "\",\"node_id\":\"" + node_id(0) + rip.json + ",\"created_at\":\"" + Time.now.toString + "\"}"
      new GunnySack((uir.get._1 + uir.get._2), json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> newnode_metadataVal), Map((newnode_bindex, bvalue))).some
    }
  }

  /*
   * create new Request with the existing 'Nodename' of the nodename provide as input.
   * A index name nodeID will point to the "nodes" bucket
   */
  def createforExistNode(input: String): ValidationNel[Throwable, Option[(Map[String, String], String, NodeAppDefns)]] = {
    import models.Defns._
    play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySackforExistNode(input) leftMap { err: NonEmptyList[Throwable] =>
      err
    }).flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          val adf_result = parse(gs.get.value).extract[AppDefnsResult]
          play.api.Logger.debug(("%-20s -->[%s]%nwith%n----%n%s").format("AppDefns.created successfully", "input", input))
          maybeGS match {
            case Some(thatGS) => ((Map[String, String](("Id" -> thatGS.key))), adf_result.node_name, adf_result.appdefns).some.successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("AppDefns.created success", "Scaliak returned => None. Thats OK."))
              ((Map[String, String](("Id" -> gs.get.key), ("Action" -> DefnType.APP.toString), ("Args" -> List().toString))), adf_result.node_name, adf_result.appdefns).some.successNel[Throwable]
            }
          }
        }
    }
  }

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, Option[node_id].
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the GunnySack object is built.
   * If the node_id is send by the Node model. It then yield the GunnySack object.
   */
  private def mkGunnySackforNewNode(input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns:json", input))

    //Does this failure get propagated ? I mean, the input json parse fails ? I don't think so.
    //This is a potential bug.
    val ripNel: ValidationNel[Throwable, AppDefnsInputforNewNode] = (Validation.fromTryCatch {
      parse(input).extract[AppDefnsInputforNewNode]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns:rip", ripNel))

    for {
      rip <- ripNel
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "adf").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for aor, and uir to filter the None case. confirm it during function testing.
      val bvalue = Set(rip.node_id)
      val json = "{\"id\": \"" + (uir.get._1 + uir.get._2) + rip.json + ",\"created_at\":\"" + Time.now.toString + "\"}"

      new GunnySack((uir.get._1 + uir.get._2), json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> newnode_metadataVal), Map((newnode_bindex, bvalue))).some
    }
  }

  /*
   * create new Node with the 'name' of the node provide as input.
   * A index name accountID will point to the "accounts" bucket
   */
  def createforNewNode(input: String): ValidationNel[Throwable, Option[Tuple3[String, String, NodeAppDefns]]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySackforNewNode(input) leftMap { err: NonEmptyList[Throwable] =>
      err
    }).flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          val adf_result = parse(gs.get.value).extract[AppDefnsResult]
          play.api.Logger.debug(("%-20s -->[%s]%nwith%n----%n%s").format("AppDefns.created successfully", "input", input))
          maybeGS match {
            case Some(thatGS) => (thatGS.key, adf_result.node_name, adf_result.appdefns).some.successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("AppDefns.created success", "Scaliak returned => None. Thats OK."))
              (gs.get.key, adf_result.node_name, adf_result.appdefns).some.successNel[Throwable]
            }
          }
        }
    }
  }

  /**
   * List all the app defns for a list of appdefns id for a particular node.
   */
  def findByReqName(defNameList: Option[List[String]]): ValidationNel[Error, AppDefnsResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefinition", "findByReqName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("nodeNameList", defNameList))
    (defNameList map {
      _.map { defName =>
        play.api.Logger.debug(("%-20s -->[%s]").format("Name", defName))
        (riak.fetch(defName) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(defName, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              (Validation.fromTryCatch {
                parse(xs.value).extract[AppDefnsResult]
              } leftMap { t: Throwable =>
                new ResourceItemNotFound(defName, t.getMessage)
              }).toValidationNel.flatMap { j: AppDefnsResult =>
                Validation.success[Error, AppDefnsResults](nels(j.some)).toValidationNel
              }
            }
            case None => Validation.failure[Error, AppDefnsResults](new ResourceItemNotFound(defName, "")).toValidationNel
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((AppDefnsResults.empty).successNel[Error])(_ +++ _)
    }).head //return the folded element in the head. 

  }

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email, 
   * the nodenames are listed on the index (account.id) in bucket `Nodes`.
   * Using a "nodename" as key, return a list of ValidationNel[List[AppDefnsResult]] 
   * Takes an email, and returns a Future[ValidationNel, List[Option[AppDefnsResult]]]
   */
  def findByNodeName(nodeNameList: Option[List[String]]): ValidationNel[Throwable, AppDefnsResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("nodeNameList", nodeNameList))
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Error, AppDefnsResults]] {
      ((((for {
        nelnr <- (Nodes.findByNodeName(nodeNameList) leftMap { t: NonEmptyList[Throwable] => t })
      } yield {
        //this is ugly, since what we receive from Nodes always contains one None. We need to filter
        //that. This is justa  hack for now. It calls for much more elegant soln.
        (nelnr.list filter (nelwop => nelwop.isDefined) map { nelnor: Option[NodeResult] =>
          (if (nelnor.isDefined) { //we only want to use the Some, ignore None. Hence a pattern match wasn't used here.
            val bindex = BinIndex.named("")
            val bvalue = Set("")
            val metadataVal = "Nodes-name"
            play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns", nelnor))
            new GunnySack("appdefnsId", nelnor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
              None, Map(metadataKey -> metadataVal), Map((bindex, bvalue)))
          }).asInstanceOf[GunnySack]
        })
      }) leftMap { t: NonEmptyList[Throwable] => t } flatMap (({ gs: List[GunnySack] =>
        gs.map { ngso: GunnySack => riak.fetchIndexByValue(ngso) }
      }) map {
        _.foldRight((List[String]()).successNel[Throwable])(_ +++ _)
      })) map { nm: List[String] =>
        play.api.Logger.debug("------------->" + nm)
        (if (!nm.isEmpty) findByReqName(nm.some) else
          new ResourceItemNotFound(nodeNameList.map(m => m.mkString("[", ",", "]")).get, "AppDefns = nothing found.").failureNel[AppDefnsResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    res.getOrElse(new ResourceItemNotFound(nodeNameList.map(m => m.mkString("[", ",", "]")).get, "AppDefns = nothing found.").failureNel[AppDefnsResults])

  }

  /*
   * An IO wrapped finder using an email. Upon fetching the node results for an email, 
   * the nodeids are listed in bucket `Requests`.
   * Using a "requestid" as key, return a list of ValidationNel[List[RequestResult]] 
   * Takes an email, and returns a Future[ValidationNel, List[Option[RequestResult]]]
   */
  def findByEmail(email: String): ValidationNel[Throwable, AppDefnsResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns", "findByEmail:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Error, AppDefnsResults]] {
      ((((for {
        nelnr <- (Nodes.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      } yield {
        play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns", "fetched nodes by email"))
        //this is ugly, since what we receive from Nodes always contains one None. We need to filter
        //that. This is justa  hack for now. It calls for much more elegant soln.
        (nelnr.list filter (nelwop => nelwop.isDefined) map { nelnor: Option[NodeResult] =>
          val bindex = BinIndex.named("")
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
        (if (!nm.isEmpty) findByReqName(nm.some) else
          new ResourceItemNotFound(email, "definitions = nothing found.").failureNel[AppDefnsResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns", res))
    res.getOrElse(new ResourceItemNotFound(email, "definitions = nothing found.").failureNel[AppDefnsResults])
  }

  private def updateGunnySack(input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    val defnInput: ValidationNel[Throwable, AppDefnsUpdateInput] = (Validation.fromTryCatch {
      parse(input).extract[AppDefnsUpdateInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure  

    play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns", defnInput))
    for {
      nir <- defnInput
      aor <- (models.AppDefns.findByReqName(List(nir.appdefn_id).some) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      val onir = aor.list filter (nelwop => nelwop.isDefined) map { nelnor: Option[AppDefnsResult] =>
        (if (nelnor.isDefined) { //we only want to use the Some, ignore None. Hence a pattern match wasn't used here.          
          AppDefnsResult(nelnor.get.id, nelnor.get.node_id, nelnor.get.node_name, nelnor.get.appdefns, nelnor.get.created_at)
        }).asInstanceOf[AppDefnsResult]
      }
      val bvalue = Set(onir(0).node_id)
      val jsonobj = AppDefnsResult(onir(0).id, onir(0).node_id, onir(0).node_name, valueChange(onir(0).appdefns, nir.env_sh), Time.now.toString())

      play.api.Logger.debug(("%-20s -->[%s]").format("formatted node store", jsonobj.json))
      val json = jsonobj.json
      new GunnySack(onir(0).id, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> newnode_metadataVal), Map((newnode_bindex, bvalue))).some
    }
  }

  def update(input: String): ValidationNel[Throwable, Option[(Map[String, String], String, NodeAppDefns)]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.AppDefns", "update:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))
     import models.Defns._
    (updateGunnySack(input) leftMap { err: NonEmptyList[Throwable] =>
      err
    }).flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          val adf_result = parse(gs.get.value).extract[AppDefnsResult]
          play.api.Logger.debug(("%-20s -->[%s]%nwith%n----%n%s").format("AppDefns.created successfully", "input", input))
          maybeGS match {
            case Some(thatGS) => ((Map[String, String](("Id" -> thatGS.key))), adf_result.node_name, adf_result.appdefns).some.successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("AppDefns.created success", "Scaliak returned => None. Thats OK."))
              ((Map[String, String](("Id" -> gs.get.key), ("Action" -> DefnType.APP.toString), ("Args" -> List().toString))), adf_result.node_name, adf_result.appdefns).some.successNel[Throwable]
            }
          }
        }
    }

  }

  private def valueChange(appdefns: NodeAppDefns, env_sh: String): NodeAppDefns = {

    //val appdefns = parse(old_value).extract[NodeAppDefns]
    new NodeAppDefns(appdefns.timetokill, appdefns.metered, appdefns.logging, appdefns.runtime_exec, env_sh)
  }

}