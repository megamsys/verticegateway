/* 
** Copyright [2013-2014] [Megam Systems]
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
import models.AccountResult

/**
 * @author ram
 *
 */
class AccountResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[AccountResult] {

  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val EmailKey = "email"
  protected val APIKey = "api_key"
  protected val AuthorityKey = "authority"
  protected val CreatedAtKey ="created_at"  

  override implicit val writer = new JSONW[AccountResult] {

    override def write(h: AccountResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(EmailKey, toJSON(h.email)) ::
          JField(APIKey, toJSON(h.api_key)) ::
          JField(AuthorityKey, toJSON(h.authority))    ::
          JField(CreatedAtKey, toJSON(h.created_at))   :: 
          JField(JSONClazKey, toJSON("Megam::Account")) :: Nil)
    }
  }

  override implicit val reader = new JSONR[AccountResult] {

    override def read(json: JValue): Result[AccountResult] = {
      val idField = field[String](IdKey)(json)
      val emailField = field[String](EmailKey)(json)
      val apiKeyField = field[String](APIKey)(json)
      val authorityField = field[String](AuthorityKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| emailField |@| apiKeyField |@| authorityField |@| createdAtField) {
        (id: String, email: String, apikey: String, authority: String, created_at: String) =>
          new AccountResult(id, email, apikey, authority, created_at)
      }
    }
  }
}