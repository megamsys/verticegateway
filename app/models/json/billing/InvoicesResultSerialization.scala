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
import models.billing.{ InvoicesResult }
/**
 * @author rajthilak
 *
 */
class InvoicesResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[InvoicesResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ

  protected val IdKey = "id"
  protected val AccountsIdKey = "accounts_id"
  protected val FromDateKey = "from_date"
  protected val ToDateKey = "to_date"
  protected val MonthKey = "month"
  protected val BillTypeKey = "bill_type"
  protected val BillingAmountKey = "billing_amount"
  protected val CurrencyTypeKey = "currency_type"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[InvoicesResult] {

    override def write(h: InvoicesResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
        JField(AccountsIdKey, toJSON(h.accounts_id)) ::
          JField(JSONClazKey, toJSON("Megam::Invoices")) ::
          JField(FromDateKey, toJSON(h.from_date)) ::
           JField(ToDateKey, toJSON(h.to_date)) ::
           JField(MonthKey, toJSON(h.month)) ::
          JField(BillTypeKey, toJSON(h.bill_type)) ::
          JField(BillingAmountKey, toJSON(h.billing_amount)) ::
          JField(CurrencyTypeKey, toJSON(h.currency_type)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[InvoicesResult] {

    override def read(json: JValue): Result[InvoicesResult] = {

      val idField = field[String](IdKey)(json)
      val accountsIdField = field[String](AccountsIdKey)(json)
      val fromDateField = field[String](FromDateKey)(json)
      val toDateField = field[String](ToDateKey)(json)
      val monthField = field[String](MonthKey)(json)
      val billTypeField = field[String](BillTypeKey)(json)
      val billingamountField = field[String](BillingAmountKey)(json)
      val currencytypeField = field[String](CurrencyTypeKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

    (idField |@| accountsIdField |@| fromDateField |@| toDateField |@| monthField |@| billTypeField |@| billingamountField |@| currencytypeField |@| createdAtField) {
         (id: String, accounts_id: String, from_date: String, to_date: String,  month: String, bill_type: String, billing_amount: String, currency_type: String, created_at: String) =>
          new InvoicesResult(id, accounts_id, from_date, to_date, month, bill_type, billing_amount, currency_type, created_at)
      }
    }
  }
}
