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
 * @author morpheyesh
 *
 */
class PromosResultSerialization(charset: Charset = UTF8Charset) extends io.megam.json.SerializationBase[PromosResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ

  protected val IdKey = "id"
  protected val CodeKey = "code"
  protected val AmountKey = "amount"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[PromosResult] {

    override def write(h: PromosResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(JSONClazKey, toJSON("Megam::Promos")) ::
          JField(CodeKey, toJSON(h.code)) ::
          JField(AmountKey, toJSON(h.amount)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[PromosResult] {

    override def read(json: JValue): Result[PromosResult] = {

      val idField = field[String](IdKey)(json)
      val codeField = field[String](CodeKey)(json)
      val amountField = field[String](AmountKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| codeField |@| amountField |@| createdAtField) {
        (id: String, code: String, amount: String, created_at: String) =>
          new PromosResult(id, code, amount, created_at)
      }
    }
  }
}
