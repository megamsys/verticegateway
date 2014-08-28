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
import models.tosca.{ Assembly, Components }

/**
 * @author ram
 *
 */
class AssemblySerialization(charset: Charset = UTF8Charset) extends SerializationBase[Assembly] {

//  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val NameKey = "name"
  protected val ComponentsKey = "components"
  protected val PoliciesKey = "policies"
  protected val InputsKey = "inputs"
  protected val OperationsKey = "operations"
    
  override implicit val writer = new JSONW[Assembly] {
    
 import ComponentsSerialization.{ writer => ComponentsWriter }
 
    override def write(h: Assembly): JValue = {
      JObject(
   //     JField(JSONClazKey, toJSON("Megam::Assembly")) ::
          JField(NameKey, toJSON(h.name)) ::
          //JField(ComponentsKey, toJSON(h.components)(ComponentsWriter)) ::
          JField(ComponentsKey, toJSON(h.components)) ::
          JField(PoliciesKey, toJSON(h.policies)) ::
          JField(InputsKey, toJSON(h.inputs)) ::
          JField(OperationsKey, toJSON(h.operations)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[Assembly] {
    
     import ComponentsSerialization.{ reader => ComponentsReader }

    override def read(json: JValue): Result[Assembly] = {
      val nameField = field[String](NameKey)(json)
//      val componentsField = field[Components](ComponentsKey)(json)(ComponentsReader)
      val componentsField = field[String](ComponentsKey)(json)
      val policiesField = field[String](PoliciesKey)(json)
      val inputsField = field[String](InputsKey)(json)  
      val operationsField = field[String](OperationsKey)(json)

      (nameField |@| componentsField |@| policiesField |@| inputsField |@| operationsField) {
        //(name: String, components: Components, policies: String, inputs: String, operations: String) =>
          (name: String, components: String, policies: String, inputs: String, operations: String) =>
          new Assembly(name, components, policies, inputs, operations)
      }
    }
  }
}