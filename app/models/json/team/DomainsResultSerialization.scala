/*
** Copyright [2013-2016] [Megam Systems]
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
package models.json.team

import scalaz._
import scalaz.NonEmptyList._
import scalaz.Validation
import scalaz.Validation._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.util.Date
import java.nio.charset.Charset
import io.megam.auth.funnel.FunnelErrors._
import controllers.Constants._
import models.team.{ DomainsResult }
/**
 * @author morpheyesh
 *
 */
class DomainsResultSerialization(charset: Charset = UTF8Charset) extends io.megam.json.SerializationBase[DomainsResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ

  protected val NameKey = "name"
  protected val IdKey = "id"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[DomainsResult] {

    override def write(h: DomainsResult): JValue = {
      JObject(

        JField(NameKey, toJSON(h.name)) ::
          JField(IdKey, toJSON(h.id)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[DomainsResult] {

    override def read(json: JValue): Result[DomainsResult] = {

      val idField = field[String](IdKey)(json)
      val nameField = field[String](NameKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nameField |@| createdAtField) {
        (id: String, name: String, created_at: String) =>
          new DomainsResult(id, name, created_at)
      }
    }
  }
}
