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
import models.{ PredefCloudAccess }

/**
 * @author ram
 *
 */

object PredefCloudAccessSerialization extends SerializationBase[PredefCloudAccess] {

  protected val SSHKey = "ssh_key"
  protected val IdentityFileKey = "identity_file"
  protected val SSHUserKey = "ssh_user"
  protected val VaultLocationKey = "vault_location"
  protected val SshPubLocationKey = "sshpub_location"

  override implicit val writer = new JSONW[PredefCloudAccess] {

    override def write(h: PredefCloudAccess): JValue = {
      JObject(
        JField(SSHKey, toJSON(h.ssh_key)) ::
          JField(IdentityFileKey, toJSON(h.identity_file)) ::
          JField(SSHUserKey, toJSON(h.ssh_user)) :: 
          JField(VaultLocationKey, toJSON(h.vault_location))  :: 
          JField(SshPubLocationKey, toJSON(h.sshpub_location))  :: Nil)
    }
  }

  override implicit val reader = new JSONR[PredefCloudAccess] {

    override def read(json: JValue): Result[PredefCloudAccess] = {
      val sshKeyField = field[String](SSHKey)(json)
      val identityFileField = field[String](IdentityFileKey)(json)
      val sshUserField = field[String](SSHUserKey)(json)
      val vaultlocationField = field[String](VaultLocationKey)(json)
      val sshpublocationField = field[String](SshPubLocationKey)(json)

      (sshKeyField |@| identityFileField |@| sshUserField |@| vaultlocationField |@| sshpublocationField) {
        (ssh_key: String, identity_file: String, ssh_user, vault_location: String, sshpub_location: String) =>
          new PredefCloudAccess(ssh_key, identity_file, ssh_user, vault_location, sshpub_location)
      }
    }
  }
}