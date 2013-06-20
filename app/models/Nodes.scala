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
import com.stackmob.scaliak._
import com.basho.riak.client.query.indexes.{ RiakIndexes, IntIndex, BinIndex }
import com.basho.riak.client.http.util.{ Constants => RiakConstants }
import models._
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import org.megam.common.uid._
import net.liftweb.json.scalaz.JsonScalaz.field
/**
 * @author ram
 *
 */

case class NodeInput(nod_name: String, command: String, predefs: NodePredefs)

case class NodeFinal(id: String, accounts_ID: String, request: NodeRequest, predefs: NodePredefs)

case class NodeRequest(req_id: String, command: String) {
  val rstatus = "submitted"
  val getRequestJson = "\"req_id\":\"" + req_id + "\",\"command\":\"" + command + "\",\"status\":\"" + rstatus + "\""
}
case class NodePredefs(rails: String, scm: String, db: String, queue: String) {
  val getPredefsJson = "\"rails\":\"" + rails + "\",\"scm\":\"" + scm + "\",\"db\":\"" + db + "\",\"queue\":\"" + queue + "\""
}

case class NodeMachines(name: String, public: String, cpuMetrics: String)

object Nodes extends NodesHelper {

  implicit val formats = DefaultFormats
  private lazy val riak: GSRiak = GSRiak(MConfig.riakurl, "nodes")
  /*
   * 
   * create new ACCOUNT with secondery index
   * In this case the secondary index is accountID from "accounts" bucket
   */
  def create(input: String, acc_id: String): ValidationNel[Error, Option[NodeFinal]] = {
    val metadataKey = "Node"
    val metadataVal = "New Node Creation"
    val inputJson = parse(input)
    val m = inputJson.extract[NodeInput]
    val bindex = BinIndex.named("accountId")
    val bvalue = Set(acc_id)

    val id = getUID
    val req_id = getUID
    val predefsJson = (m.predefs).getPredefsJson
    val json = "{\"id\": \"" + id + "\",\"accounts_ID\":\"" + acc_id + "\",\"request\":{" + NodeRequest(req_id, m.command).getRequestJson + "} ,\"predefs\":{" + predefsJson + "}}"
    val storeValue = riak.store(new GunnySack(m.nod_name, json, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))
    storeValue match {
      case Success(msg) => Validation.success[Error, Option[NodeFinal]](None).toValidationNel
      case Failure(err) => Validation.failure[Error, Option[NodeFinal]](new Error("Node.create: Not Implemented.")).toValidationNel
    }
  }

  /*
   * fetch the object using their key from bucket
   */
  def findByKey(key: String): ValidationNel[Error, Option[NodeFinal]] = {
    riak.fetch(key) match {
      case Success(msg) => {
        val caseValue = msg.get
        val json = parse(caseValue.value)
        val m = json.extract[NodeFinal]
        Validation.success[Error, Option[NodeFinal]](Some(m)).toValidationNel
      }
      case Failure(err) => Validation.failure[Error, Option[NodeFinal]](new Error("Node.findById: Not Implemented.")).toValidationNel
    }
  }

  /*
   * Index on ID
   * fetch the object using index
   */
  def findById(id: String): ValidationNel[Error, List[ValidationNel[Error, Option[NodeFinal]]]] = {
    val metadataKey = "Nodes"
    val metadataVal = "Nodes-name"
    val bindex = BinIndex.named("")
    val bvalue = Set("")
    val fetchValue = riak.fetchIndexByValue(new GunnySack("accountID", id, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))

    fetchValue match {
      case Success(msg) => {
        val result = msg.map(a => {
          Nodes.findByKey(a)
        })
        /*Nodes.findByKey(a) match {
            case Success(msg) => {
              val m = msg.get
              m
            }
            case Failure(_) => ""
          }
        })*/
        Validation.success[Error, List[ValidationNel[Error, Option[NodeFinal]]]](result).toValidationNel
      }
      case Failure(err) => Validation.failure[Error, List[ValidationNel[Error, Option[NodeFinal]]]](new Error("Email is not already exists")).toValidationNel
    }
  }

  /*
   * get all nodes for single user
   * this was only return the nodes name
   */
  def getNodes(id: String): ValidationNel[Error, List[String]] = {
    val metadataKey = "Nodes"
    val metadataVal = "Nodes-name"
    val bindex = BinIndex.named("")
    val bvalue = Set("")
    val fetchValue = riak.fetchIndexByValue(new GunnySack("accountID", id, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))

    fetchValue match {
      case Success(msg) => {
        Validation.success[Error, List[String]](msg).toValidationNel
      }
      case Failure(err) => Validation.failure[Error, List[String]](new Error("Email is not already exists")).toValidationNel
    }
  }
}

