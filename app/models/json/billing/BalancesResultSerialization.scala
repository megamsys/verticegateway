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
class BalancesResultSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[BalancesResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ

  protected val IdKey = "id"
  protected val NameKey = "name"
  protected val CreditKey = "credit"
  protected val CreatedAtKey = "created_at"
  protected val UpdatedAtKey = "updated_at"

  override implicit val writer = new JSONW[BalancesResult] {

    override def write(h: BalancesResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(JSONClazKey, toJSON("Megam::Balances")) ::
          JField(CreditKey, toJSON(h.credit)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
          JField(UpdatedAtKey, toJSON(h.updated_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[BalancesResult] {

    override def read(json: JValue): Result[BalancesResult] = {

      val idField = field[String](IdKey)(json)
      val nameField = field[String](NameKey)(json)
      val creditField = field[String](CreditKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)
      val updatedAtField = field[String](UpdatedAtKey)(json)

      (idField |@| nameField |@| creditField |@| createdAtField |@| updatedAtField) {
        (id: String, name: String, credit: String, created_at: String, updated_at: String) =>
          new BalancesResult(id, name, credit, created_at, updated_at)
      }
    }
  }
}
