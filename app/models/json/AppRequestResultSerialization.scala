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
import models.{AppRequestResult}

/**
 * @author rajthilak
 *
 */
class AppRequestResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[AppRequestResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val AppIDKey = "app_id"
  protected val AppNameKey = "app_name"
  protected val ActionKey = "action"  
  protected val CreatedAtKey ="created_at" 

  override implicit val writer = new JSONW[AppRequestResult] {
    
    override def write(h: AppRequestResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(AppIDKey, toJSON(h.app_id)) ::
          JField(AppNameKey, toJSON(h.app_name)) ::
          JField(JSONClazKey, toJSON("Megam::AppRequest")) ::
          JField(ActionKey, toJSON(h.action)) ::        
          JField(CreatedAtKey, toJSON(h.created_at))   :: Nil)
    }
  }

  override implicit val reader = new JSONR[AppRequestResult] {
    
    override def read(json: JValue): Result[AppRequestResult] = {
      val idField = field[String](IdKey)(json)
      val appIdField = field[String](AppIDKey)(json)
      val appNameField = field[String](AppNameKey)(json)
      val actionField = field[String](ActionKey)(json)      
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| appIdField |@| appNameField |@| actionField |@| createdAtField) {
        (id: String, app_id: String, app_name: String, action: String, created_at: String) =>
          new AppRequestResult(id, app_id, app_name, action, created_at)
      }
    }
  }
}