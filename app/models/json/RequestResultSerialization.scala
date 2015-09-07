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
import models.{RequestResult}

/**
 * @author ram
 *
 */
class RequestResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[RequestResult] {
protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val CatIDKey = "cat_id"
  protected val CatTypeKey = "cattype"
  protected val NameKey = "name"
  protected val ActionKey = "action"
  protected val CategoryKey = "category"
  protected val CreatedAtKey ="created_at" 

  override implicit val writer = new JSONW[RequestResult] {

    override def write(h: RequestResult): JValue = {
      JObject(
      JField(IdKey, toJSON(h.id)) ::
          JField(CatIDKey, toJSON(h.cat_id)) ::
          JField(CatTypeKey, toJSON(h.cattype)) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(JSONClazKey, toJSON("Megam::Requests")) ::
          JField(ActionKey, toJSON(h.action)) ::
          JField(CategoryKey, toJSON(h.category)) ::
          JField(CreatedAtKey, toJSON(h.created_at))   :: Nil)
    }
  }

  override implicit val reader = new JSONR[RequestResult] {

    override def read(json: JValue): Result[RequestResult] = {
      val idField = field[String](IdKey)(json)
      val catIdField = field[String](CatIDKey)(json)
      val catTypeField = field[String](CatTypeKey)(json)
      val nameField = field[String](NameKey)(json)
      val actionField = field[String](ActionKey)(json)
       val categoryField = field[String](CategoryKey)(json)     
      val createdAtField = field[String](CreatedAtKey)(json)

(idField |@| catIdField |@| catTypeField |@| nameField |@| actionField |@| categoryField |@| createdAtField) {
        (id: String, cat_id: String, cattype: String, name: String, action: String, category: String, created_at: String) =>
          new RequestResult(id, cat_id, cattype, name, action, category, created_at)
      }
    }
  }
}