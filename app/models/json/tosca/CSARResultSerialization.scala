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
package models.json.tosca

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
import models.tosca.CSARResult

/**
 * @author rajthilak
 *
 */
class CSARResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[CSARResult] {

  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val DescKey = "desc"
  protected val LinkKey = "link"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[CSARResult] {

    override def write(h: CSARResult): JValue = {
      play.api.Logger.debug(("%-20s -->[%s]").format("csar.serialization", "entry"))

      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(DescKey, toJSON(h.desc)) ::
          JField(JSONClazKey, toJSON("Megam::CSAR")) ::
          JField(LinkKey, toJSON(h.link)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[CSARResult] {

    override def read(json: JValue): Result[CSARResult] = {
      val idField = field[String](IdKey)(json)
      val descField = field[String](DescKey)(json)
      val linkField = field[String](LinkKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| descField |@| linkField |@| createdAtField) {
        (id: String, desc: String, link: String, created_at: String) =>
          new CSARResult(id, desc, link, created_at)
      }
    }
  }
}