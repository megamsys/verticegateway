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
import models.tosca.{ Assembly, ComponentsList, PoliciesList, OutputsList }

/**
 * @author rajthilak
 *
 */
class AssemblySerialization(charset: Charset = UTF8Charset) extends SerializationBase[Assembly] {

//  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val NameKey = "name"
  protected val ComponentsKey = "components"
  protected val PoliciesKey = "policies"
  protected val InputsKey = "inputs"
  protected val OperationsKey = "operations"
  protected val OutputsKey = "outputs"
    
  override implicit val writer = new JSONW[Assembly] {
    
 import ComponentsListSerialization.{ writer => ComponentsListWriter }
 import PoliciesListSerialization.{ writer => PoliciesListWriter }
 import OutputsListSerialization.{ writer => OutputsListWriter }
 
    override def write(h: Assembly): JValue = {
      JObject(
   //     JField(JSONClazKey, toJSON("Megam::Assembly")) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(ComponentsKey, toJSON(h.components)(ComponentsListWriter)) ::
          JField(PoliciesKey, toJSON(h.policies)(PoliciesListWriter)) ::
          JField(InputsKey, toJSON(h.inputs)) ::
          JField(OperationsKey, toJSON(h.operations)) :: 
          JField(OutputsKey, toJSON(h.outputs)(OutputsListWriter)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[Assembly] {
    
     import ComponentsListSerialization.{ reader => ComponentsListReader }
     import PoliciesListSerialization.{reader => PoliciesListReader }
     import OutputsListSerialization.{reader => OutputsListReader }

    override def read(json: JValue): Result[Assembly] = {
      val nameField = field[String](NameKey)(json)
      val componentsField = field[ComponentsList](ComponentsKey)(json)(ComponentsListReader)
      val policiesField = field[PoliciesList](PoliciesKey)(json)(PoliciesListReader)
      val inputsField = field[String](InputsKey)(json)  
      val operationsField = field[String](OperationsKey)(json)
      val outputsField = field[OutputsList](OutputsKey)(json)(OutputsListReader)

      (nameField |@| componentsField |@| policiesField |@| inputsField |@| operationsField |@| outputsField) {
          (name: String, components: ComponentsList, policies: PoliciesList, inputs: String, operations: String, outputs: OutputsList) =>
          new Assembly(name, components, policies, inputs, operations, outputs)
      }
    }
  }
}