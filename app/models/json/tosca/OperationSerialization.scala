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
import models.tosca.{ Operation, KeyValueList }

/**
 * @author rajthilak
 *
 */

class OperationSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[Operation] {

  protected val OperationTypeKey = "operation_type"
  protected val DescriptionKey = "description"
  protected val PropertiesKey = "properties"

  override implicit val writer = new JSONW[Operation] {

    import models.json.tosca.KeyValueListSerialization.{ writer => KeyValueListWriter }

    override def write(h: Operation): JValue = {
      JObject(
        JField(OperationTypeKey, toJSON(h.operation_type)) ::
          JField(DescriptionKey, toJSON(h.description)) ::
          JField(PropertiesKey, toJSON(h.properties)(KeyValueListWriter)) ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[Operation] {

     import models.json.tosca.KeyValueListSerialization.{ reader => KeyValueListReader }

    override def read(json: JValue): Result[Operation] = {
      val operationtypeField = field[String](OperationTypeKey)(json)
      val descriptionField = field[String](DescriptionKey)(json)
      val propertiesField = field[KeyValueList](PropertiesKey)(json)(KeyValueListReader)

      (operationtypeField |@| descriptionField |@| propertiesField) {
        (operationtype: String, description: String, properties: KeyValueList) =>
          new Operation(operationtype, description, properties)
      }
    }
  }
}
