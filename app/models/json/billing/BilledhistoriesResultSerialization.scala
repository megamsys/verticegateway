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
import models.billing.{ BilledhistoriesResult }

/**
 * @author rajthilak
 *
 */
class BilledhistoriesResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[BilledhistoriesResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ

  protected val IdKey = "id"
  protected val AccountsIdKey = "accounts_id"
  protected val AssemblyIdKey = "assembly_id"
  protected val BillTypeKey = "bill_type"
  protected val BillingAmountKey = "billing_amount"
  protected val CurrencyTypeKey = "currency_type"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[BilledhistoriesResult] {

    override def write(h: BilledhistoriesResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(AccountsIdKey, toJSON(h.accounts_id)) ::
          JField(JSONClazKey, toJSON("Megam::Billedhistories")) ::
           JField(AssemblyIdKey, toJSON(h.assembly_id)) ::
          JField(BillTypeKey, toJSON(h.bill_type)) ::
          JField(BillingAmountKey, toJSON(h.billing_amount)) ::
          JField(CurrencyTypeKey, toJSON(h.currency_type)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[BilledhistoriesResult] {

    override def read(json: JValue): Result[BilledhistoriesResult] = {

      val idField = field[String](IdKey)(json)
      val accountsIdField = field[String](AccountsIdKey)(json)
      val assemblyIdField = field[String](AssemblyIdKey)(json)
      val billTypeField = field[String](BillTypeKey)(json)
      val billingamountField = field[String](BillingAmountKey)(json)
      val currencytypeField = field[String](CurrencyTypeKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| accountsIdField |@| assemblyIdField |@| billTypeField |@| billingamountField |@| currencytypeField |@| createdAtField) {
        (id: String, accounts_id: String, assembly_id: String, bill_type: String, billing_amount: String, currency_type: String, created_at: String) =>
          new BilledhistoriesResult(id, accounts_id, assembly_id, bill_type, billing_amount, currency_type, created_at)
      }
    }
  }
}
