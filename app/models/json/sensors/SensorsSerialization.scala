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
package models.json.sensors

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
import models.tosca.{ Sensors, Payload }

/**
 * @author ranjitha
 *
 */
class SensorsSerialization(charset: Charset = UTF8Charset) extends io.megam.json.SerializationBase[Sensors] {
  protected val SensorTypeKey = "sensor_type"
  protected val PayloadKey = "payload"

  override implicit val writer = new JSONW[Sensors] {

    import PayloadSerialization.{ writer => PayloadWriter }

    override def write(h: Sensors): JValue = {
      JObject(
        JField(SensorTypeKey, toJSON(h.sensor_type)) ::
          JField(PayloadKey, toJSON(h.payload)(PayloadWriter)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[Sensors] {

    import PayloadSerialization.{ reader => PayloadReader }

    override def read(json: JValue): Result[Sensors] = {
      val sensortypeField = field[String](SensorTypeKey)(json)
      val payloadField = field[Payload](PayloadKey)(json)(PayloadReader)

      (sensortypeField |@| payloadField) {
        (sensor_type: String, payload: Payload) =>
          new Sensors(sensor_type, payload)
      }
    }
  }
}
