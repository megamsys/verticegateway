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
package models.json.billing

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
import models.billing.{ BillingsResult }

/**
 * @author rajthilak
 *
 */
class BillingsResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[BillingsResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ

  protected val IdKey = "id"
  protected val AccountIdKey = "account_id"
  protected val Line1Key = "line1"
  protected val Line2Key = "line2"
  protected val CountryCodeKey = "country_code"
  protected val PostalCodeKey = "postal_code"
  protected val StateKey = "state"
  protected val PhoneKey = "phone"
  protected val BillTypeKey = "bill_type"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[BillingsResult] {

    override def write(h: BillingsResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(AccountIdKey, toJSON(h.account_id)) ::
          JField(JSONClazKey, toJSON("Megam::Billings")) ::
          JField(Line1Key, toJSON(h.line1)) ::
          JField(Line2Key, toJSON(h.line2)) ::
          JField(CountryCodeKey, toJSON(h.country_code)) ::
          JField(PostalCodeKey, toJSON(h.postal_code)) ::
          JField(StateKey, toJSON(h.state)) ::
          JField(PhoneKey, toJSON(h.phone)) ::
          JField(BillTypeKey, toJSON(h.bill_type)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[BillingsResult] {

    override def read(json: JValue): Result[BillingsResult] = {

      val idField = field[String](IdKey)(json)
      val accountIdField = field[String](AccountIdKey)(json)
      val line1Field = field[String](Line1Key)(json)
      val line2Field = field[String](Line2Key)(json)
      val countrycodeField = field[String](CountryCodeKey)(json)
      val postalcodeField = field[String](PostalCodeKey)(json)
      val stateField = field[String](StateKey)(json)
      val phoneField = field[String](PhoneKey)(json)
      val billTypeField = field[String](BillTypeKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| accountIdField |@| line1Field |@| line2Field |@| countrycodeField |@| postalcodeField |@| stateField |@| phoneField |@| billTypeField |@| createdAtField) {
        (id: String, account_id: String, line1: String, line2: String, country_code: String, postal_code: String, state: String, phone: String, bill_type: String, created_at: String) =>
          new BillingsResult(id, account_id, line1, line2, country_code, postal_code, state, phone, bill_type, created_at)
      }
    }
  }
}