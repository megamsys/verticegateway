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
import models.billing._

/**
 * @author rajthilak
 *
 */
class SubscriptionsResultSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[SubscriptionsResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ

  protected val IdKey = "id"
  protected val AccountsIdKey = "accounts_id"
  protected val AssemblyIdKey = "assembly_id"
  protected val StartDateKey = "start_date"
  protected val EndDateKey = "end_date"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[SubscriptionsResult] {

    override def write(h: SubscriptionsResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
        JField(AccountsIdKey, toJSON(h.accounts_id)) ::
        JField(AssemblyIdKey, toJSON(h.assembly_id)) ::
          JField(StartDateKey, toJSON(h.start_date)) ::
          JField(JSONClazKey, toJSON("Megam::Subscriptions")) ::
          JField(EndDateKey, toJSON(h.end_date)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[SubscriptionsResult] {

    override def read(json: JValue): Result[SubscriptionsResult] = {

      val idField = field[String](IdKey)(json)
      val accountsidField = field[String](AccountsIdKey)(json)
      val assemblyidField = field[String](AssemblyIdKey)(json)
      val startdateField = field[String](StartDateKey)(json)
      val enddateField = field[String](EndDateKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| accountsidField |@| assemblyidField |@| startdateField |@| enddateField |@| createdAtField) {
        (id: String, accounts_id: String, assembly_id: String, start_date: String, end_date: String, created_at: String) =>
          new SubscriptionsResult(id, accounts_id, assembly_id, start_date, end_date, created_at)
      }
    }
  }
}
