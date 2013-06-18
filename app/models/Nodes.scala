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
import controllers.stack.MConfig
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

case class NodeInput(command: String, predefs: NodePredefs)

case class NodeFinal(id: String, acc_id: String, request: NodeRequest, predefs: NodePredefs)

case class NodeRequest(id: String, command: String) {
  val rstatus = "submitted"
    println("+++++++++++++++++++Request Entry")
  //def this(id: String, command: String) = this(id, command, "submitted")
  val getRequestJson = "\"req_id\":\"" + id + "\",\"command\":\"" + command + "\",\"status\":\"" + rstatus + "\""
}
case class NodePredefs(rails: String, scm: String, db: String, queue: String) {
  println("++++++++++++++++++Predefs Entry")
  val getPredefsJson = "\"rails\":\"" + rails + "\",\"scm\":\"" + scm + "\",\"db\":\"" + db + "\",\"queue\":\"" + queue + "\""
}

case class NodeMachines(name: String, public: String, cpuMetrics: String)

object Nodes {
  implicit val formats = DefaultFormats
  private lazy val riak: GSRiak = GSRiak(MConfig.riakurl, "nodes")
  private val SFHOST = MConfig.snowflakeHost
  private val SFPORT: Int = MConfig.snowflakePort
  val key = "mykey15"
  /*
   * put the value in riak bucket
   */
  def getUID: String = {
    val id = UID(SFHOST, SFPORT, "nod").get
    val res: UniqueID = id match {
      case Success(uid) => {
        println("------>" + uid)
        uid
      }
      case Failure(error) => { None }
    }
    (res.get._1 + res.get._2)
  }
  def create(input: String, acc_id: String): ValidationNel[Error, Option[NodeFinal]] = {
    val metadataKey = "Field"
    val metadataVal = "1002"
    val inputJson = parse(input)
    val m = inputJson.extract[NodeInput]
    val bindex = BinIndex.named("accountID")
    val bvalue = Set(acc_id)
   
    val id = getUID
    val req_id = getUID
    val predefsJson = parse((m.predefs).getPredefsJson)
    println("++++++++++++++++++++++++++++"+predefsJson)
    val json = "{\"id\": \"" + id + "\",\"accounts_ID\":\"" + acc_id + "\",\"request\":\"" + NodeRequest(req_id, m.command).getRequestJson+ "\" ,\"predefs\":\"" + predefsJson +"\"}"                 
    
    val storeValue = riak.store(new GunnySack(key, json, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))
    storeValue match {
      case Success(msg) => Validation.success[Error, Option[NodeFinal]](None).toValidationNel
      case Failure(err) => Validation.failure[Error, Option[NodeFinal]](new Error("Node.create: Not Implemented.")).toValidationNel
    }
  }

  /*
   * fetch the object using their key from bucket
   */
  def findById(key: String): ValidationNel[Error, Option[NodeFinal]] = {
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

  /**
   * Index on email
   */
  def findByEmail(email: String): ValidationNel[Error, Option[NodeFinal]] = {
    val metadataKey = "Field"
    val metadataVal = "1002"
    val bindex = BinIndex.named("")
    val bvalue = Set("")
    val fetchValue = riak.fetchIndexByValue(new GunnySack("accountID", email, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))

    fetchValue match {
      case Success(msg) => {
        val key1 = msg match {
          case List(x) => x
        }
        Nodes.findById(key1)
      }
      case Failure(err) => Validation.failure[Error, Option[NodeFinal]](new Error("Email is not already exists")).toValidationNel
    }
  }

}

