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
import models.{ AppDefnsResult, NodeAppDefns }

/**
 * @author ram
 *
 */
class AppDefnsResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[AppDefnsResult] {

  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val NodeIdKey = "node_id"
  protected val AppDefnsKey = "appdefns"
  protected val CreatedAtKey ="created_at" 
    
  override implicit val writer = new JSONW[AppDefnsResult] {

    import NodeAppDefnsSerialization.{ writer => NodeAppDefnsWriter }

    override def write(h: AppDefnsResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
        JField(JSONClazKey, toJSON("Megam::AppDefns")) ::
          JField(NodeIdKey, toJSON(h.node_id)) ::
          JField(AppDefnsKey, toJSON(h.appdefns)(NodeAppDefnsWriter)) :: 
          JField(CreatedAtKey, toJSON(h.created_at)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[AppDefnsResult] {

    import NodeAppDefnsSerialization.{ reader => NodeAppDefnsReader }

    override def read(json: JValue): Result[AppDefnsResult] = {
      val idField = field[String](IdKey)(json)
      val nodeidField = field[String](NodeIdKey)(json)
      val appdefnsField = field[NodeAppDefns](AppDefnsKey)(json)(NodeAppDefnsReader)  
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nodeidField |@| appdefnsField |@| createdAtField) {
        (id: String, node_id: String, appdefns: NodeAppDefns, created_at: String) =>
          new AppDefnsResult(id, node_id, appdefns, created_at)
      }
    }
  }
}