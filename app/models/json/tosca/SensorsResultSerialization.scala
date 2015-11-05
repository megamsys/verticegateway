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
package models.json.tosca

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
import models.tosca.{ SensorsResult, Payload }

/**
 * @author ranjitha
 *
 */
class SensorsResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[SensorsResult] {

  protected val JSONClazKey = controllers.Constants.JSON_CLAZ

  protected val IdKey = "id"
  protected val SensorTypeKey = "sensor_type"
  protected val PayloadKey = "payload"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[SensorsResult] {

    import models.json.tosca.PayloadSerialization.{ writer => PayloadWriter }

    override def write(h: SensorsResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::

          JField(JSONClazKey, toJSON("Megam::Sensors")) ::
          JField(SensorTypeKey, toJSON(h.sensor_type)) ::
          JField(PayloadKey, toJSON(h.payload)(PayloadWriter)) ::

          JField(CreatedAtKey, toJSON(h.created_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[SensorsResult] {

    import PayloadSerialization.{ reader => PayloadReader }

    override def read(json: JValue): Result[SensorsResult] = {

      val idField = field[String](IdKey)(json)
      val sensorTypeField = field[String](SensorTypeKey)(json)
      val payloadField = field[Payload](PayloadKey)(json)(PayloadReader)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| sensorTypeField |@| payloadField |@| createdAtField) {
        (id: String, sensor_type: String, payload: Payload, created_at: String) =>
          new SensorsResult(id, sensor_type, payload, created_at)
      }
    }
  }
}
