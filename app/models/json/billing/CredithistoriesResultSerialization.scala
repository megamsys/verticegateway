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
import io.megam.auth.funnel.FunnelErrors._
import controllers.Constants._
import models.billing._

/**
 * @author rajthilak
 *
 */
class CredithistoriesResultSerialization(charset: Charset = UTF8Charset) extends io.megam.json.SerializationBase[CredithistoriesResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ

  protected val IdKey = "id"
  protected val AccountsIdKey = "accounts_id"
  protected val BillTypeKey = "bill_type"
  protected val CreditAmountKey = "credit_amount"
  protected val CurrencyTypeKey = "currency_type"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[CredithistoriesResult] {

    override def write(h: CredithistoriesResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(AccountsIdKey, toJSON(h.accounts_id)) ::
          JField(JSONClazKey, toJSON("Megam::Credithistories")) ::
          JField(BillTypeKey, toJSON(h.bill_type)) ::
          JField(CreditAmountKey, toJSON(h.credit_amount)) ::
          JField(CurrencyTypeKey, toJSON(h.currency_type)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[CredithistoriesResult] {

    override def read(json: JValue): Result[CredithistoriesResult] = {

      val idField = field[String](IdKey)(json)
      val accountsIdField = field[String](AccountsIdKey)(json)
      val billTypeField = field[String](BillTypeKey)(json)
      val creditamountField = field[String](CreditAmountKey)(json)
      val currencytypeField = field[String](CurrencyTypeKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| accountsIdField |@| billTypeField |@| creditamountField |@| currencytypeField |@| createdAtField) {
        (id: String, accounts_id: String, bill_type: String, credit_amount: String, currency_type: String, created_at: String) =>
          new CredithistoriesResult(id, accounts_id, bill_type, credit_amount, currency_type, created_at)
      }
    }
  }
}
