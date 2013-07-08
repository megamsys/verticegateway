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

  /*
   * 
   * create new ACCOUNT with secondery index
   * In this case the secondary index is accountID from "accounts" bucket
   */
  def create(input: String, email: String): ValidationNel[Error, Option[NodeResult]] = {
    play.api.Logger.debug("models.Account create: entry\n" + input)

    val nodeInput = (Validation.fromTryCatch {
      parse(input).extract[NodeInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel

    for {
      m <- nodeInput
      ao <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Error] => t })
    } yield ao flatMap { ar: AccountResult =>
      {
        UID(MConfig.snowflakeHost, MConfig.snowflakePort, "nod").get match {
          case Success(uid) => {
            val bvalue = Set(ar.id)
            val predefsJson = (m.predefs).getPredefsJson
            val json = "{\"id\": \"" + (uid.get._1 + uid.get._2) + "\",\"accounts_ID\":\"" + ar.id + "\",\"request\":{" + NodeRequest(uid.get._1 + uid.get._2, m.command).getRequestJson + "} ,\"predefs\":{" + predefsJson + "}}"

            val storeValue = riak.store(new GunnySack(m.node_name, json, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> newnode_metadataVal), Map((newnode_bindex, bvalue))))
            storeValue match {
              case Success(succ) => Validation.success[Error, Option[NodeResult]] {
                (parse(succ.getOrElse(new GunnySack()).value).extract[NodeResult].some)
              }.toValidationNel
              case Failure(err) => Validation.failure[Error, Option[NodeResult]](
                new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))).toValidationNel
            }

          }
          case Failure(err) => Validation.failure[Error, Option[NodeResult]](
            new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))).toValidationNel
        }
      }
    }

    /*.flatMap { m: NodeInput =>
      Accounts.findByEmail(email) leftMap { t: NonEmptyList[Error] => t }
        .flatMap  ao: AccountResult =>
          UID(MConfig.snowflakeHost, MConfig.snowflakePort, "nod").get match {
            case Success(uid) => {
              val bvalue = Set(ao.id)
              val predefsJson = (m.predefs).getPredefsJson
              val json = "{\"id\": \"" + (uid.get._1 + uid.get._2) + "\",\"accounts_ID\":\"" + ao.id + "\",\"request\":{" + NodeRequest(uid.get._1 + uid.get._2, m.command).getRequestJson + "} ,\"predefs\":{" + predefsJson + "}}"
              val a = for {
                storeValue <- riak.store(new GunnySack(m.node_name, json, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> newnode_metadataVal), Map((newnode_bindex, bvalue))))
              } yield storeValue flatMap { succ: GunnySack =>
                // Validation.success[Error, Option[NodeResult]] {
                (parse(succ.some.getOrElse(new GunnySack()).value).extract[NodeResult].some)
                // }.toValidationNel
              }
              err1: NonEmptyList[Throwable]  =>  Some("")
            }
            case Failure(err) => { err =>
              Validation.failure[Error, Option[NodeResult]](
                new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))).toValidationNel
            }
          }

        }
    }*/
  }
  /**
   * Measure the duration to accomplish a usecase by listing an user who has 10 nodes using single threaded(current behaviour)
   * https://github.com/twitter/util (Copy the Duration code into a new perf pkg in megam_common.)
   * val elapsed: () => Duration = Stopwatch.start()
   * val duration: Duration = elapsed()
   *
   * TODO: Converting to Async Futures.
   * ----------------------------------
   * Then takes an input list of nodenames which will return a Future[ValidationNel[Error, Option[NodeResult]]]
   * The intensive computation is the riak fetch call.
   *  make as many future as the nodeName.
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
  def findByEmail(email: String): ValidationNel[Error, List[Option[NodeResult]]] = {
    Logger.debug("models.Nodes findByEmail: entry:" + email)

    Accounts.findByEmail(email) leftMap { t: Error => Validation.failure[Error, Option[NodeResult]](t) }.toValidationNel
      .flatMap { ao: AccountResult =>

        val account = Accounts.findByEmail(email).getOrElse(throw new Error("Doomed"))
        val metadataVal = "Nodes-name"
        val bindex = BinIndex.named("")
        val bvalue = Set("")
        for {
          nodeNamesList <- (riak.fetchIndexByValue(new GunnySack("accountID", account.id, RiakConstants.CTYPE_TEXT_UTF8,
            None, Map(metadataKey -> metadataVal), Map((bindex, bvalue)))) leftMap
            { t: NonEmptyList[Throwable] => new ServiceUnavailableError(email, (t.list.map(m => m.getMessage)).mkString("\n"))
            })
          if (nodeNamesList.size > 0)
        } yield (nodeNamesList) map { nodeName => findByNodeName(nodeName) }
      }
  }

  /*
   * get all nodes for single user
   * this was only return the nodes name
   */
  def listNodesByEmail(email: String): ValidationNel[Error, Option[List[String]]] = {
    Logger.debug("models.Nodes listNodesByEmail: entry:" + email)

    Accounts.findByEmail(email) leftMap { t: Error => Validation.failure[Error, Option[NodeResult]](t) }.toValidationNel
      .flatMap { ao: AccountResult =>
        val metadataVal = "Nodes-name"
        val bindex = BinIndex.named("")
        val bvalue = Set("")
        for {
          nodeNamesList <- (riak.fetchIndexByValue(new GunnySack("accountID", account.id, RiakConstants.CTYPE_TEXT_UTF8,
            None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))
            leftMap { t: NonEmptyList[Throwable] =>
              new ServiceUnavailableError(email, (t.list.map(m => m.getMessage)).mkString("\n"))
            })
          if (nodeNamesList.size > 0)
        } yield (Validation.success[Error, Option[List[String]]](nodeNamesList.some))
      }
  }

}

