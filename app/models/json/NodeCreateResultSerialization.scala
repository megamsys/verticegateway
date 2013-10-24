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
import models.{ NodeCreateResult}
import models.NodeStatusType._
import org.megam.common.enumeration._

/**
 * @author ram
 *
 */
class NodeCreateResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[NodeCreateResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val NodeKKey = "key"  
    protected val NodeTypeKey = "node_type"
      protected val ReqIdKey = "req_id"
  protected val ReqTypeKey = "req_type"  
    
  override implicit val writer = new JSONW[NodeCreateResult] {  
    
    
    override def write(h: NodeCreateResult): JValue = {
      JObject(
        JField(NodeKKey, toJSON(h.key)) ::          
          JField(NodeTypeKey, toJSON(h.node_type)) ::
          JField(JSONClazKey, toJSON("Megam::NodeCreate")) ::
          JField(ReqTypeKey, toJSON(h.req_type)) ::
              JField(ReqIdKey, toJSON(h.req_id)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[NodeCreateResult] {    
    
    
    override def read(json: JValue): Result[NodeCreateResult] = {
     val keyField = field[String](NodeKKey)(json)
      val nodeTypeField = field[String](NodeTypeKey)(json)      
      val reqtypeField = field[String](ReqTypeKey)(json)    
      val reqidField = field[String](ReqIdKey)(json) 

      (keyField |@| nodeTypeField |@| reqidField |@| reqtypeField ) {
        (id: String, node_type: String, req_id: String, req_type: String) =>
          new NodeCreateResult(id, node_type, req_id, req_type)
      }
    }
  }
}