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
import models.base.{ SshKeysResult }

/**
 * @author rajthilak
 *
 */
class SshKeysResultSerialization(charset: Charset = UTF8Charset) extends io.megam.json.SerializationBase[SshKeysResult] {
  protected val JSONClazKey = models.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val NameKey = "name"
  protected val OrgIdKey = "org_id"
  protected val PrivateKey = "privatekey"
  protected val PublicKey = "publickey"
  protected val CreatedAtKey ="created_at"

  override implicit val writer = new JSONW[SshKeysResult] {

    override def write(h: SshKeysResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(OrgIdKey, toJSON(h.org_id)) ::
          JField(JSONClazKey, toJSON("Megam::SshKeys")) ::
          JField(PrivateKey, toJSON(h.privatekey)) ::
          JField(PublicKey, toJSON(h.publickey)) ::
          JField(CreatedAtKey, toJSON(h.created_at))   ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[SshKeysResult] {

    override def read(json: JValue): Result[SshKeysResult] = {
      val idField = field[String](IdKey)(json)
      val nameField = field[String](NameKey)(json)
      val orgIdField = field[String](OrgIdKey)(json)
      val privateKeyField = field[String](PrivateKey)(json)
      val publicKeyField = field[String](PublicKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nameField |@| orgIdField  |@| privateKeyField |@| publicKeyField |@| createdAtField) {
        (id: String, name: String, orgId: String, privatekey: String, publickey: String, created_at: String) =>
          new SshKeysResult(id, name, orgId, privatekey, publickey, created_at)
      }
    }
  }
}
