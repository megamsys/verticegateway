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
import Scalaz._
import controllers.stack._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import models._
import com.twitter.util.Time
import com.stackmob.scaliak._
import com.basho.riak.client.query.indexes.{ RiakIndexes, IntIndex, BinIndex }
import com.basho.riak.client.http.util.{ Constants => RiakConstants }
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz.{ Result, UncategorizedError }
import java.nio.charset.Charset

/**
 * @author ram
 * This would actually be a link to the Nodes bucket. which would allow us to use link-walking
 */
case class BoltRequestInput(req_type: String, node_name: String, boltdefns_id: String, lc_apply: String, lc_additional: String, lc_when: String) {
  val json = "\"node_name\":\"" + node_name + "\",\"boltdefns_id\":\"" + boltdefns_id + "\",\"req_type\":\"" + req_type + "\",\"lc_apply\":\"" + lc_apply + "\",\"lc_additional\":\"" + lc_additional + "\",\"lc_when\":\"" + lc_when + "\""
}

case class BoltRequestResult(id: String, node_id: String, node_name: String, boltdefns_id: String, req_type: String, lc_apply: String, lc_additional: String, lc_when: String, created_at: String) {
  val json = "{\"id\": \"" + id + "\",\"node_id\": \"" + node_id + "\",\"node_name\":\"" + node_name + "\",\"boltdefns_id\":\"" + boltdefns_id + "\",\"req_type\":\"" + req_type + "\",\"lc_apply\":\"" + lc_apply + "\",\"lc_additional\":\"" + lc_additional +
    "\",\"lc_when\":\"" + lc_when + "\",\"created_at\":\"" + created_at + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.BoltRequestResultSerialization
    val nrsser = new BoltRequestResultSerialization()
    toJSON(this)(nrsser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object BoltRequestResult {

  def apply = new BoltRequestResult(new String(), new String(), new String(), new String(), new String(), new String(), new String(), new String(), new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[BoltRequestResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.BoltRequestResultSerialization
    val nrsser = new BoltRequestResultSerialization()
    fromJSON(jValue)(nrsser.reader)
  }

  def fromJson(json: String): Result[BoltRequestResult] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object BoltRequests {

  implicit val formats = DefaultFormats

  implicit def BoltRequestResultsSemigroup: Semigroup[BoltRequestResults] = Semigroup.instance((f1, f2) => f1.append(f2))

  private def riak: GSRiak = GSRiak(MConfig.riakurl, "boltreqs")

  val metadataKey = "BoltRequest"
  val newnode_metadataVal = "New BoltRequest Creation"
  val newnode_bindex = BinIndex.named("nodesId")
  val boltdefns_bindex = BinIndex.named("boltdefnsId")
  
  /**
   * A private method which chains computation to make GunnySack for existing node when provided with an input json, Option[node_name].
   * parses the json, and converts it to requestinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the GunnySack object is built.
   * If the node_name is send by the Node model. It then yield the GunnySack object.
   */
  private def mkGunnySack(input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.BoltRequests", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("models.BoltRequests:json", input))

    //Does this failure get propagated ? I mean, the input json parse fails ? I don't think so.
    //This is a potential bug.
    val ripNel: ValidationNel[Throwable, BoltRequestInput] = (Validation.fromTryCatch {
      parse(input).extract[BoltRequestInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    play.api.Logger.debug(("%-20s -->[%s]").format("models.BoltRequests:rip", ripNel))
    for {
      rip <- ripNel
      aor <- (models.Nodes.findByNodeName(List(rip.node_name).some) leftMap { t: NonEmptyList[Throwable] => t })
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "bpr").get leftMap { ut: NonEmptyList[Throwable] => ut })      
    } yield {     
      val tuple_node = aor.list filter (nelwop => nelwop.isDefined) map { nelnor: Option[NodeResult] =>
        (if (nelnor.isDefined) { //we only want to use the Some, ignore None. Hence a pattern match wasn't used here.
          Tuple2(nelnor.get.id, nelnor.get.boltdefnsid)
        }).asInstanceOf[Tuple2[String, String]]
      }      
      val bvalue = Set(tuple_node(0)._1)
      val boltdefns_bvalue = Set(rip.boltdefns_id)
      val json = "{\"id\": \"" + (uir.get._1 + uir.get._2) + "\",\"node_id\":\"" + tuple_node(0)._1 + "\"," + rip.json + ",\"created_at\":\"" + Time.now.toString + "\"}"
      new GunnySack((uir.get._1 + uir.get._2), json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> newnode_metadataVal), Map((newnode_bindex, bvalue),(boltdefns_bindex, boltdefns_bvalue))).some
    }    
  }  

  /*
   * create new BoltRequest with the 'Nodename' of the node provide as input.
   * A index name BoltreqID will point to the "Boltreqs" bucket
   */
  def create(input: String): ValidationNel[Throwable, Option[Tuple2[Map[String,String], String]]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.BoltRequests", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(input) leftMap { err: NonEmptyList[Throwable] =>
      err
    }).flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          val req_result = parse(gs.get.value).extract[BoltRequestResult]
          play.api.Logger.debug(("%-20s -->[%s]%nwith%n----%n%s").format("BoltRequest.created successfully", "input", input))
          maybeGS match {
            case Some(thatGS) => Tuple2(Map[String,String](("Id" ->thatGS.key)), req_result.node_name).some.successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("BoltRequest.created success", "Scaliak returned => None. Thats OK."))
            Tuple2(Map[String,String](("Id"-> gs.get.key), ("Action"-> req_result.req_type), ("Args"-> List().toString)), req_result.node_name).some.successNel[Throwable]
            }
          }
        }
    }
  }

    
  /**
   * List all the requests for the requestlist.
   */
  def findByReqName(reqNameList: Option[List[String]]): ValidationNel[Error, BoltRequestResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.BoltRequest", "findByReqName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("nodeNameList", reqNameList))
    (reqNameList map {
      _.map { reqName =>
        play.api.Logger.debug(("%-20s -->[%s]").format("reqName", reqName))
        (riak.fetch(reqName) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(reqName, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              (Validation.fromTryCatch {
                parse(xs.value).extract[BoltRequestResult]
              } leftMap { t: Throwable =>
                new ResourceItemNotFound(reqName, t.getMessage)
              }).toValidationNel.flatMap { j: BoltRequestResult =>
                Validation.success[Error, BoltRequestResults](nels(j.some)).toValidationNel  
              }
            }
            case None => Validation.failure[Error, BoltRequestResults](new ResourceItemNotFound(reqName, "")).toValidationNel
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((BoltRequestResults.empty).successNel[Error])(_ +++ _)
    }).head //return the folded element in the head. 

  }

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email, 
   * the nodenames are listed on the index (account.id) in bucket `Nodes`.
   * Using a "nodename" as key, return a list of ValidationNel[List[BoltRequestResult]] 
   * Takes an email, and returns a Future[ValidationNel, List[Option[BoltRequestResult]]]
   */
  def findByNodeName(nodeNameList: Option[List[String]]): ValidationNel[Throwable, BoltRequestResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.BoltRequest", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("nodeNameList", nodeNameList))
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Error, BoltRequestResults]] {
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
            play.api.Logger.debug(("%-20s -->[%s]").format("models.BoltRequest", nelnor))
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
        (if (!nm.isEmpty) findByReqName(nm.some) else
          new ResourceItemNotFound(nodeNameList.map(m => m.mkString("[", ",", "]")).get, "application requests = nothing found.").failureNel[BoltRequestResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    res.getOrElse(new ResourceItemNotFound(nodeNameList.map(m => m.mkString("[", ",", "]")).get, "application requests = nothing found.").failureNel[BoltRequestResults])

  } 

}