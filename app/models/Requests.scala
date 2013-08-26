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
case class RequestInput(node_id: String, node_name: String, command: String) {
  val json = "\",\"node_id\":\"" + node_id + "\",\"node_name\":\"" + node_name + "\",\"command\":\"" + command + "\""
}

case class RequestResult(id: String, node_id: String, node_name: String, command: String) {
  val json = "{\"id\": \"" + id + "\",\"node_id\":\"" + node_id + "\",\"node_name\":\"" + node_name +
    "\",\"command\":\"" + command + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.RequestResultSerialization
    val nrsser = new RequestResultSerialization()
    toJSON(this)(nrsser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object RequestResult {

  def apply = new RequestResult(new String(), new String(), new String(), new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[RequestResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.RequestResultSerialization
    val nrsser = new RequestResultSerialization()
    fromJSON(jValue)(nrsser.reader)
  }

  def fromJson(json: String): Result[RequestResult] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Requests {

  implicit val formats = DefaultFormats

  implicit def RequestResultsSemigroup: Semigroup[RequestResults] = Semigroup.instance((f1, f2) => f1.append(f2))

  private lazy val riak: GSRiak = GSRiak(MConfig.riakurl, "requests")

  val metadataKey = "Request"
  val newnode_metadataVal = "New Request Creation"
  val newnode_bindex = BinIndex.named("nodeId")

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, Option[node_id].
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the GunnySack object is built.
   * If the node_id is send by the Node model. It then yield the GunnySack object.
   */
  private def mkGunnySack(input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Requests", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    //Does this failure get propagated ? I mean, the input json parse fails ? I don't think so.
    //This is a potential bug.
    val ripNel: ValidationNel[Throwable, RequestInput] = (Validation.fromTryCatch {
      parse(input).extract[RequestInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    play.api.Logger.debug(("%-20s -->[%s]").format("ripNel", ripNel))

    for {
      rip <- ripNel
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "rip").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for aor, and uir to filter the None case. confirm it during function testing.
      val bvalue = Set(rip.node_id)
      val json = "{\"id\": \"" + (uir.get._1 + uir.get._2) + rip.json + "}"

      new GunnySack((uir.get._1 + uir.get._2), json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> newnode_metadataVal), Map((newnode_bindex, bvalue))).some
    }
  }

  /*
   * create new Node with the 'name' of the node provide as input.
   * A index name accountID will point to the "accounts" bucket
   */
  def create(input: String): ValidationNel[Throwable, Option[Tuple2[String, String]]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Requests", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(input) leftMap { err: NonEmptyList[Throwable] =>
      err
    }).flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          val req_result = parse(gs.get.value).extract[RequestResult]
          play.api.Logger.debug(("%-20s -->[%s]%nwith%n----%n%s").format("Request.created successfully", "input", input))
          maybeGS match {
            case Some(thatGS) => (thatGS.key, req_result.command).some.successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("Request.created success", "Scaliak returned => None. Thats OK."))
              (gs.get.key, req_result.command).some.successNel[Throwable]
            }
          }
        }
    }
  }

  /**
   * List all the requests for the nodenamelist.
   */
  def findByReqName(reqNameList: Option[List[String]]): ValidationNel[Error, RequestResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Request", "findByReqName:Entry"))
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
                parse(xs.value).extract[RequestResult]
              } leftMap { t: Throwable =>
                new ResourceItemNotFound(reqName, t.getMessage)
              }).toValidationNel.flatMap { j: RequestResult =>
                Validation.success[Error, RequestResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ? 
              }
            }
            case None => Validation.failure[Error, RequestResults](new ResourceItemNotFound(reqName, "")).toValidationNel
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((RequestResults.empty).successNel[Error])(_ +++ _)
    }).head //return the folded element in the head. 

  }

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email, 
   * the nodenames are listed on the index (account.id) in bucket `Nodes`.
   * Using a "nodename" as key, return a list of ValidationNel[List[RequestResult]] 
   * Takes an email, and returns a Future[ValidationNel, List[Option[RequestResult]]]
   */
  def findByNodeName(nodeNameList: Option[List[String]]): ValidationNel[Throwable, RequestResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Request", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("nodeNameList", nodeNameList))
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Error, RequestResults]] {
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
            play.api.Logger.debug(("%-20s -->[%s]").format("models.Request1", nelnor))
            new GunnySack("nodeId", nelnor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
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
          new ResourceItemNotFound(nodeNameList.map(m => m.mkString("[", ",", "]")).get, "requests = nothing found.").failureNel[RequestResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    res.getOrElse(new ResourceItemNotFound(nodeNameList.map(m => m.mkString("[", ",", "]")).get, "requests = nothing found.").failureNel[RequestResults])

  }

  /*
   * An IO wrapped finder using an email. Upon fetching the node results for an email, 
   * the nodeids are listed in bucket `Requests`.
   * Using a "requestid" as key, return a list of ValidationNel[List[RequestResult]] 
   * Takes an email, and returns a Future[ValidationNel, List[Option[RequestResult]]]
   */
  def findByEmail(email: String): ValidationNel[Throwable, RequestResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Request", "findByEmail:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Error, RequestResults]] {
      ((((for {
        nelnr <- (Nodes.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      } yield {
        play.api.Logger.debug(("%-20s -->[%s]").format("models.Request", "fetched nodes by email"))
        //this is ugly, since what we receive from Nodes always contains one None. We need to filter
        //that. This is justa  hack for now. It calls for much more elegant soln.
        (nelnr.list filter (nelwop => nelwop.isDefined) map { nelnor: Option[NodeResult] =>
          val bindex = BinIndex.named("")
          val bvalue = Set("")
          val metadataVal = "Nodes-name"
          play.api.Logger.debug(("%-20s -->[%s]").format("models.Request1", nelnor))
          new GunnySack("nodeId", nelnor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
            None, Map(metadataKey -> metadataVal), Map((bindex, bvalue)))
        })
      }) leftMap { t: NonEmptyList[Throwable] => t } flatMap (({ gs: List[GunnySack] =>
        gs.map { ngso: GunnySack => riak.fetchIndexByValue(ngso) }
      }) map {
        _.foldLeft((List[String]()).successNel[Throwable])(_ +++ _)
      })) map { nm: List[String] =>
        (if (!nm.isEmpty) findByReqName(nm.some) else
          new ResourceItemNotFound(email, "requests = nothing found.").failureNel[RequestResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Request", res))
    res.getOrElse(new ResourceItemNotFound(email, "requests = nothing found.").failureNel[RequestResults])
  }

}