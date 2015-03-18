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
import models.PredefResult

/**
 * @author ram
 *
 */
class PredefResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[PredefResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val NameKey = "name"
  protected val ProviderKey = "provider"
  protected val ProviderRoleKey = "provider_role"
  protected val BuildMonkeyKey = "build_monkey"  
    protected val RunTimeKey = "runtime_exec" 
    protected val CreatedAtKey ="created_at"

  override implicit val writer = new JSONW[PredefResult] {

    override def write(h: PredefResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(ProviderKey, toJSON(h.provider)) ::
          JField(ProviderRoleKey, toJSON(h.provider_role)) ::
          JField(BuildMonkeyKey, toJSON(h.build_monkey)) ::  
          JField(RunTimeKey, toJSON(h.runtime_exec)) ::
          JField(CreatedAtKey, toJSON(h.created_at))   ::
          JField(JSONClazKey, toJSON("Megam::Predef")) :: Nil)
    }
  }

  override implicit val reader = new JSONR[PredefResult] {

    override def read(json: JValue): Result[PredefResult] = {
      val idField = field[String](IdKey)(json)
      val nameField = field[String](NameKey)(json)
      val providerField = field[String](ProviderKey)(json)
      val providerRoleField = field[String](ProviderRoleKey)(json)
      val buildMonkeyField = field[String](BuildMonkeyKey)(json) 
      val runTimeField = field[String](RunTimeKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nameField |@| providerField |@| providerRoleField |@| buildMonkeyField |@| runTimeField |@| createdAtField) {
        (id: String, name: String, provider: String, provider_role: String, build_monkey: String, runtime_exec: String, created_at: String) =>
          new PredefResult(id, name, provider, provider_role, build_monkey, runtime_exec, created_at)
      }
    }
  }
}