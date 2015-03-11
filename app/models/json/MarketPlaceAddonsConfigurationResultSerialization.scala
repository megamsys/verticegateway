/* 
** Copyright [2013-2015] [Megam Systems]
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
import models.{ MarketPlaceAddonsConfigurationResult, MarketPlaceAddonsConfig }

/**
 * @author rajthilak
 *
 */
class MarketPlaceAddonsConfigurationResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[MarketPlaceAddonsConfigurationResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val NodeIdKey = "node_id"
  protected val NodeNameKey = "node_name"  
  protected val ConfigKey = "config"  
  protected val CreatedAtKey ="created_at"

  override implicit val writer = new JSONW[MarketPlaceAddonsConfigurationResult] {
   import MarketPlaceAddonsConfigSerialization.{ writer => MarketPlaceAddonsConfigWriter }

    override def write(h: MarketPlaceAddonsConfigurationResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
        JField(NodeIdKey, toJSON(h.node_id)) ::
          JField(NodeNameKey, toJSON(h.node_name)) ::          
          JField(JSONClazKey, toJSON("Megam::MarketPlaceAddons")) ::          
          JField(ConfigKey, toJSON(h.config)(MarketPlaceAddonsConfigWriter)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[MarketPlaceAddonsConfigurationResult] {
     import MarketPlaceAddonsConfigSerialization.{ reader => MarketPlaceAddonsConfigReader }
     
    override def read(json: JValue): Result[MarketPlaceAddonsConfigurationResult] = {
      val idField = field[String](IdKey)(json)
      val nodeIdField = field[String](NodeIdKey)(json)
      val nodeNameField = field[String](NodeNameKey)(json)     
      val configField = field[MarketPlaceAddonsConfig](ConfigKey)(json)(MarketPlaceAddonsConfigReader) 
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nodeIdField |@| nodeNameField |@| configField |@| createdAtField) {
        (id: String, node_id: String, node_name: String, config: MarketPlaceAddonsConfig, created_at: String) =>
          new MarketPlaceAddonsConfigurationResult(id, node_id, node_name, config, created_at)
      }
    }
  }
}