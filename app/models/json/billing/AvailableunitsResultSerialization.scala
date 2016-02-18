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
import models.Constants._
import models.billing._
/**
 * @author rajthilak
 *
 */
class AvailableunitsResultSerialization(charset: Charset = UTF8Charset) extends io.megam.json.SerializationBase[AvailableunitsResult] {
  protected val JSONClazKey = models.Constants.JSON_CLAZ

  protected val IdKey = "id"
  protected val NameKey = "name"
  protected val DurationKey = "duration"
  protected val ChargesPerDurationKey = "charges_per_duration"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[AvailableunitsResult] {

    override def write(h: AvailableunitsResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(JSONClazKey, toJSON("Megam::Availableunits")) ::
          JField(DurationKey, toJSON(h.duration)) ::
          JField(ChargesPerDurationKey, toJSON(h.charges_per_duration)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[AvailableunitsResult] {

    override def read(json: JValue): Result[AvailableunitsResult] = {

      val idField = field[String](IdKey)(json)
      val nameField = field[String](NameKey)(json)
      val durationField = field[String](DurationKey)(json)
      val chargesperdurationField = field[String](ChargesPerDurationKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nameField |@| durationField |@| chargesperdurationField |@| createdAtField) {
        (id: String, name: String, duration: String, charges_per_duration: String, created_at: String) =>
          new AvailableunitsResult(id, name, duration, charges_per_duration, created_at)
      }
    }
  }
}
