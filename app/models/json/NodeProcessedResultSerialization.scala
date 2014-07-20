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
package models.json

import scalaz._
import scalaz.NonEmptyList._
import scalaz.Validation
import scalaz.Validation._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.util.Date
import java.nio.charset.Charset
import controllers.funnel.FunnelErrors._
import controllers.Constants._
import controllers.funnel.SerializationBase
import models.{ NodeProcessedResult }
import models.NodeStatusType._
import org.megam.common.enumeration._

/**
 * @author ram
 *
 */
class NodeProcessedResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[NodeProcessedResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val NodeKKey = "key"
  protected val NodeTypeKey = "node_type"
  protected val ReqIdKey = "req_id"
  protected val ReqTypeKey = "req_type"
  protected val StatusKey = "status"

  override implicit val writer = new JSONW[NodeProcessedResult] {

    override def write(h: NodeProcessedResult): JValue = {
      JObject(
        JField(NodeKKey, toJSON(h.key)) ::
          JField(NodeTypeKey, toJSON(h.node_type)) ::
          JField(JSONClazKey, toJSON("Megam::NodeProcessed")) ::
          JField(ReqTypeKey, toJSON(h.req_type)) ::
          JField(ReqIdKey, toJSON(h.req_id)) :: JField(StatusKey, toJSON(h.status)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[NodeProcessedResult] {

    override def read(json: JValue): Result[NodeProcessedResult] = {
      val keyField = field[String](NodeKKey)(json)
      val nodeTypeField = field[String](NodeTypeKey)(json)
      val reqtypeField = field[String](ReqTypeKey)(json)
      val reqidField = field[String](ReqIdKey)(json)
      val statusField = field[String](StatusKey)(json)

      (keyField |@| nodeTypeField |@| reqidField |@| reqtypeField |@| statusField) {
        (id: String, node_type: String, req_id: String, req_type: String, status: String) =>
          new NodeProcessedResult(id, node_type, req_id, req_type, status)
      }
    }
  }
}