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

import play.api._
import play.api.mvc._
import scalaz._
import scalaz.NonEmptyList._
import scalaz.Validation._
import Scalaz._
import net.liftweb.json._
import controllers.stack._
import controllers.funnel.FunnelErrors._
import models._

import com.stackmob.scaliak._
import com.basho.riak.client.query.indexes.{ RiakIndexes, IntIndex, BinIndex }
import com.basho.riak.client.http.util.{ Constants => RiakConstants }
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import org.megam.common.uid._
import net.liftweb.json.scalaz.JsonScalaz.field
/**
 * @author ram
 *
 */

case class NodeInput(node_name: String, command: String, predefs: NodePredefs)

case class NodeResult(id: String, accounts_ID: String, request: NodeRequest, predefs: NodePredefs) {
  override def toString = {
    "\"id:" + id + "\"account:" + accounts_ID + "\"request:" + request.getRequestJson
  }
}

case class NodeRequest(req_id: String, command: String) {
  val rstatus = "submitted"
  val getRequestJson = "\"req_id\":\"" + req_id + "\",\"command\":\"" + command + "\",\"status\":\"" + rstatus + "\""
}
case class NodePredefs(name: String, scm: String, db: String, queue: String) {
  val getPredefsJson = "\"name\":\"" + name + "\",\"scm\":\"" + scm + "\",\"db\":\"" + db + "\",\"queue\":\"" + queue + "\""
}

case class NodeMachines(name: String, public: String, cpuMetrics: String)

object Nodes {

  implicit val formats = DefaultFormats
  private lazy val riak: GSRiak = GSRiak(MConfig.riakurl, "nodes")

  val metadataKey = "Node"
  val newnode_metadataVal = "New Node Creation"
  val newnode_bindex = BinIndex.named("accountId")

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(input: String, email: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug("models.Node mkGunnySack: entry:" + email + "\n" + input)

    val nodeInput: ValidationNel[Throwable, NodeInput] = (Validation.fromTryCatch {
      parse(input).extract[NodeInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      nir <- nodeInput
      //TO-DO: Does the leftMap make sense ? To check during function testing, by removing it.
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Error] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "nod").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for aor, and uir to filter the None case. confirm it during function testing.
      val bvalue = Set(aor.get.id)
      val predefsJson = (nir.predefs).getPredefsJson
      //TO-DO: make the json using json-scalaz (reader/writers). Review of json libs shows json-scalaz as the winner (simple to use)
      val json = "{\"id\": \"" + (uir.get._1 + uir.get._2) + "\",\"accounts_ID\":\"" + aor.get.id + "\",\"request\":{" + NodeRequest(uir.get._1 + uir.get._2, nir.command).getRequestJson + "} ,\"predefs\":{" + predefsJson + "}}"
      new GunnySack(nir.node_name, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> newnode_metadataVal), Map((newnode_bindex, bvalue))).some
    }
  }

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, the yield results in making the GunnySack object.
   */
  private def mkGunnySackF(email: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug("models.Node mkGunnySackF: entry\n" + email)
    for {
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Error] => t }) //captures failure on the left side, success on right ie the component before the (<-)
    } yield {
      //TO-DO: Where empty stuff is used, prefer GunnySack with reduced inputs.
      val bindex = BinIndex.named("")
      val bvalue = Set("")
      //TO-DO: What is the significance of the metadataVal ?
      val metadataVal = "Nodes-name"
      new GunnySack("accountID", aor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
        None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new Node with the 'name' of the node provide as input.
   * A index name accountID will point to the "accounts" bucket
   */
  def create(input: String, email: String): ValidationNel[Throwable, Option[NodeResult]] = {
    play.api.Logger.debug("models.Account create: entry\n" + input)
    (mkGunnySack(input, email) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (Validation.success[Throwable, Option[NodeResult]](parse(thatGS.value).extract[NodeResult].some)).toValidationNel
            case None         => (Validation.failure[Throwable, Option[NodeResult]](new ResourceItemNotFound(input, "The node wasn't created, store failed using 'email:'".format(email)))).toValidationNel
          }
        }
    }
  }
  /**
   * Measure the duration to accomplish a usecase by listing an user who has 10 nodes using single threaded(current behaviour)
   * https://github.com/twitter/util (Copy the Duration code into a new perf pkg in megam_common.)
   * val elapsed: () => Duration = Stopwatch.start()
   * val duration: Duration = elapsed()
   *
   * TODO: Converting to Async Futures.
   * ----------------------------------
   * takes an input list of nodenames which will return a Future[ValidationNel[Error, Option[NodeResult]]]
   * The intensive computation is the riak fetch call.
   * make as many future as the nodeName.
   *                   val futureInt = scala.concurrent.Future { intensiveComputation() }
   *                    This code should be in your controller
   *                     Async {
   *                          futureInt.map(i => Ok("Got result: " + i))
   *                      }
   */
  def findByNodeName(nodeName: String): ValidationNel[Error, Option[NodeResult]] = {
    Logger.debug("models.Nodes findByNodeName: entry:" + nodeName)
    (riak.fetch(nodeName) leftMap { t: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(nodeName, (t.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { xso: Option[GunnySack] =>
      xso match {
        case Some(xs) => {
          (Validation.fromTryCatch {
            parse(xs.value).extract[NodeResult]
          } leftMap { t: Throwable =>
            new ResourceItemNotFound(nodeName, t.getMessage)
          }).toValidationNel.flatMap { j: NodeResult =>
            Validation.success[Error, Option[NodeResult]](j.some).toValidationNel
          }
        }
        case None => Validation.failure[Error, Option[NodeResult]](new ResourceItemNotFound(nodeName, "")).toValidationNel
      }
    }
  }

  /*
   * Upon fetching account_id for an email, the nodenames is listed on the index (account.id)
   * in bucket Nodes.
   * Using a "nodename" as key, return a list of ValidationNel[List[NodeResult]] 
   * Takes an email, and returns a Future[ValidationNel, List[Option[NodeResult]]]
   */
  def findByEmail(email: String): List[ValidationNel[Throwable, Option[NodeResult]]] = {
    Logger.debug("models.Nodes findByEmail: entry:" + email)
    (for {
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
    } yield {
      val bindex = BinIndex.named("")
      val bvalue = Set("")
      val metadataVal = "Nodes-name"
      new GunnySack("accountID", aor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
        None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }) leftMap { t: NonEmptyList[Throwable] => t } flatMap {
      gs: Option[GunnySack] => riak.fetchIndexByValue(gs.get)
    } flatMap { nm => (nm.foreach (nmx => findByNodeName(nmx))) } 
  }

  /*
   * get all nodes for single user
   * this was only return the nodes name
   */
  def listNodesByEmail(email: String): ValidationNel[Throwable, List[String]] = {
    Logger.debug("models.Nodes listNodesByEmail: entry:" + email)
    (for {
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
    } yield {
      val bindex = BinIndex.named("")
      val bvalue = Set("")
      val metadataVal = "Nodes-name"
      new GunnySack("accountID", aor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
        None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }) leftMap { t: NonEmptyList[Throwable] => t } flatMap {
      gs: Option[GunnySack] => riak.fetchIndexByValue(gs.get)
    }
  }

}

