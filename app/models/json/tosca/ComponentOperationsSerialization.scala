/* 
** Copyright [2013-2014] [Megam Systems]
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
import models.tosca.{ ComponentOperations }

/**
 * @author rajthilak
 *
 */

object ComponentOperationsSerialization extends SerializationBase[ComponentOperations] {

  protected val OperationTypeKey = "operation_type"
  protected val TargetResourceKey = "target_resource"

  override implicit val writer = new JSONW[ComponentOperations] {

    override def write(h: ComponentOperations): JValue = {
      JObject(
        JField(OperationTypeKey, toJSON(h.operation_type)) ::
          JField(TargetResourceKey, toJSON(h.target_resource)) ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[ComponentOperations] {

    override def read(json: JValue): Result[ComponentOperations] = {
      val operationtypeField = field[String](OperationTypeKey)(json)
      val targetresourceField = field[String](TargetResourceKey)(json)
      
      (operationtypeField |@| targetresourceField) {
        (operation_type: String, target_resource: String) =>
          new ComponentOperations(operation_type, target_resource)
      }
    }
  }
}