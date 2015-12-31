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
import models.tosca.{ KeyValueField}

/**
 * @author rajthilak
 *
 */
class KeyValueFieldSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[KeyValueField] {

  protected val NameKey = "key"
  protected val ValueKey = "value"

  override implicit val writer = new JSONW[KeyValueField] {

    override def write(h: KeyValueField): JValue = {
      JObject(
          JField(NameKey, toJSON(h.key)) ::
          JField(ValueKey, toJSON(h.value)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[KeyValueField] {

    override def read(json: JValue): Result[KeyValueField] = {
      val nameField = field[String](NameKey)(json)
      val valueField = field[String](ValueKey)(json)

      (nameField |@| valueField ) {
          (name: String, value: String) =>
          new KeyValueField(name, value)
      }
    }
  }
}
