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
import models.{ MarketPlaceAddonsResult }

/**
 * @author rajthilak
 *
 */
class MarketPlaceAddonsResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[MarketPlaceAddonsResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val NodeIdKey = "node_id"
  protected val NodeNameKey = "node_name"
  protected val MarketPlaceIdKey = "marketplace_id"
  protected val ConfigIdKey = "config_id"  
  protected val CreatedAtKey ="created_at"

  override implicit val writer = new JSONW[MarketPlaceAddonsResult] {
   

    override def write(h: MarketPlaceAddonsResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
        JField(NodeIdKey, toJSON(h.node_id)) ::
          JField(NodeNameKey, toJSON(h.node_name)) ::
          JField(MarketPlaceIdKey, toJSON(h.marketplace_id)) ::
          JField(JSONClazKey, toJSON("Megam::MarketPlaceAddons")) ::          
          JField(ConfigIdKey, toJSON(h.config_id)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[MarketPlaceAddonsResult] {

    override def read(json: JValue): Result[MarketPlaceAddonsResult] = {
      val idField = field[String](IdKey)(json)
      val nodeIdField = field[String](NodeIdKey)(json)
      val nodeNameField = field[String](NodeNameKey)(json)
      val marketplaceIdField = field[String](MarketPlaceIdKey)(json)
      val configIdField = field[String](ConfigIdKey)(json) 
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nodeIdField |@| nodeNameField |@| marketplaceIdField |@| configIdField |@| createdAtField) {
        (id: String, node_id: String, node_name: String, marketplace_id: String, config_id: String, created_at: String) =>
          new MarketPlaceAddonsResult(id, node_id, node_name, marketplace_id, config_id, created_at)
      }
    }
  }
}