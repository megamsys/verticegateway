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
import models.{ BoltDefnsResult, NodeBoltDefns }

/**
 * @author ram
 *
 */
class BoltDefnsResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[BoltDefnsResult] {

  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val NodeIdKey = "node_id"
  protected val NodeNameKey = "node_name"
  protected val BoltDefnsKey = "boltdefns"
  protected val CreatedAtKey ="created_at" 
    
  override implicit val writer = new JSONW[BoltDefnsResult] {

    import NodeBoltDefnsSerialization.{ writer => NodeBoltDefnsWriter }

    override def write(h: BoltDefnsResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
        JField(JSONClazKey, toJSON("Megam::BoltDefns")) ::
          JField(NodeIdKey, toJSON(h.node_id)) ::
          JField(NodeNameKey, toJSON(h.node_name)) ::
          JField(BoltDefnsKey, toJSON(h.boltdefns)(NodeBoltDefnsWriter)) :: 
          JField(CreatedAtKey, toJSON(h.created_at)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[BoltDefnsResult] {

    import NodeBoltDefnsSerialization.{ reader => NodeBoltDefnsReader }

    override def read(json: JValue): Result[BoltDefnsResult] = {
      val idField = field[String](IdKey)(json)
      val nodeidField = field[String](NodeIdKey)(json)
      val nodenameField = field[String](NodeNameKey)(json)
      val boltdefnsField = field[NodeBoltDefns](BoltDefnsKey)(json)(NodeBoltDefnsReader)  
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nodeidField |@| nodenameField |@| boltdefnsField |@| createdAtField) {
        (id: String, node_id: String, node_name: String, boltdefns: NodeBoltDefns, created_at: String) =>
          new BoltDefnsResult(id, node_id, node_name, boltdefns, created_at)
      }
    }
  }
}