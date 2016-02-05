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
import io.megam.auth.funnel.FunnelErrors._
import models.Constants._
import models.base.{ SshKeyResult }

/**
 * @author rajthilak
 *
 */
class SshKeyResultSerialization(charset: Charset = UTF8Charset) extends io.megam.json.SerializationBase[SshKeyResult] {
  protected val JSONClazKey = models.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val NameKey = "name"
  protected val AccountIdKey = "accounts_id"
  protected val PathKey = "path"
    protected val CreatedAtKey ="created_at"

  override implicit val writer = new JSONW[SshKeyResult] {

    override def write(h: SshKeyResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(AccountIdKey, toJSON(h.accounts_id)) ::
          JField(JSONClazKey, toJSON("Megam::SshKey")) ::
          JField(PathKey, toJSON(h.path)) ::
          JField(CreatedAtKey, toJSON(h.created_at))   ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[SshKeyResult] {

    override def read(json: JValue): Result[SshKeyResult] = {
      val idField = field[String](IdKey)(json)
      val nameField = field[String](NameKey)(json)
      val accountIdField = field[String](AccountIdKey)(json)
      val pathField = field[String](PathKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nameField |@| accountIdField  |@| pathField |@| createdAtField) {
        (id: String, name: String, accountId: String, path: String, created_at: String) =>
          new SshKeyResult(id, name, accountId, path, created_at)
      }
    }
  }
}
