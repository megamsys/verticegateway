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
import models.{ NodeComputeAccess}
/**
 * @author ram
 *
 */
object NodeComputeAccessSerialization  extends SerializationBase[NodeComputeAccess] {
  protected val SSHKey = "ssh_key"
  protected val IdentityFileKey = "identity_file"
  protected val SSHUserKey = "ssh_user"
  protected val VaultLocationKey = "vault_location"
  protected val SshPubLocationKey = "sshpub_location"

  override implicit val writer = new JSONW[NodeComputeAccess] {

    override def write(h: NodeComputeAccess): JValue = {
      JObject(
        JField(SSHKey, toJSON(h.ssh_user)) ::
          JField(IdentityFileKey, toJSON(h.identity_file)) ::
          JField(SSHUserKey, toJSON(h.ssh_user))  ::
          JField(VaultLocationKey, toJSON(h.vault_location))  ::
          JField(SshPubLocationKey, toJSON(h.sshpub_location))  :: Nil)
    }
  }

  override implicit val reader = new JSONR[NodeComputeAccess] {

    override def read(json: JValue): Result[NodeComputeAccess] = {
      val sshkeyField = field[String](SSHKey)(json)
      val identity_Field = field[String](IdentityFileKey)(json)
      val sshuserField = field[String](SSHUserKey)(json)
      val vaultlocationField = field[String](VaultLocationKey)(json)
      val sshpublocationField = field[String](SshPubLocationKey)(json)

      (sshkeyField |@| identity_Field |@| sshuserField |@| vaultlocationField |@| sshpublocationField) {
        (sshkey: String, identity_f: String, sshuser: String, vault_location: String, sshpub_location: String) =>
          new NodeComputeAccess(sshkey,identity_f,sshuser, vault_location, sshpub_location)
      }
    }
  }
}