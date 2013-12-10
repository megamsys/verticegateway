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
import models.{ CloudToolSettingResult }

/**
 * @author rajthilak
 *
 */
class CloudToolSettingResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[CloudToolSettingResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val AccountIdKey = "accounts_id"
  protected val CloudTypeKey = "cloud_type"
  protected val RepoKey = "repo"
  protected val VaultLocationKey = "vault_location"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[CloudToolSettingResult] {

    override def write(h: CloudToolSettingResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(AccountIdKey, toJSON(h.accounts_id)) ::
          JField(JSONClazKey, toJSON("Megam::CloudToolSetting")) ::
          JField(CloudTypeKey, toJSON(h.cloud_type)) ::
          JField(RepoKey, toJSON(h.repo)) ::
          JField(VaultLocationKey, toJSON(h.vault_location)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[CloudToolSettingResult] {

    override def read(json: JValue): Result[CloudToolSettingResult] = {
      val idField = field[String](IdKey)(json)
      val accountIdField = field[String](AccountIdKey)(json)
      val cloudtypeField = field[String](CloudTypeKey)(json)
      val repoField = field[String](RepoKey)(json)
      val vaultlocationField = field[String](VaultLocationKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| accountIdField |@| cloudtypeField |@| repoField |@| vaultlocationField |@| createdAtField) {
        (id: String, accountId: String, cloud_type: String, repo: String, vault_location: String, created_at: String) =>
          new CloudToolSettingResult(id, accountId, cloud_type, repo, vault_location, created_at)
      }
    }
  }
}