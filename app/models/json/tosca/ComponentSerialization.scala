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
import models.tosca.{ Component, ComponentRequirements, Artifacts, ComponentInputs, ExResource, ComponentOperations }

/**
 * @author rajthilak
 *
 */
class ComponentSerialization(charset: Charset = UTF8Charset) extends SerializationBase[Component] {

  //protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val NameKey = "name"
  protected val ToscaTypeKey = "tosca_type"
  protected val RequirementsKey = "requirements"
  protected val InputsKey = "inputs"
  protected val ExternalManagementResourceKey = "external_management_resource"
  protected val ArtifactsKey = "artifacts"
  protected val RelatedComponentsKey = "related_components"
  protected val OperationsKey = "operations"
    
  override implicit val writer = new JSONW[Component] {
    
  //  import ComponentRequirementsSerialization.{ writer => ComponentRequirementsWriter }
    import ComponentOperationsSerialization.{ writer => ComponentOperationsWriter }
    import ComponentInputsSerialization.{ writer => ComponentInputsWriter }
  // import ExResourceSerialization.{ writer => ExResourceWriter }
    import ArtifactsSerialization.{ writer => ArtifactsWriter }
    
    override def write(h: Component): JValue = {
      JObject(
   //     JField(JSONClazKey, toJSON("Megam::Component")) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(ToscaTypeKey, toJSON(h.tosca_type)) ::
     //     JField(RequirementsKey, toJSON(h.requirements)(ComponentRequirementsWriter)) ::
            JField(RequirementsKey, toJSON(h.requirements)) ::
          JField(InputsKey, toJSON(h.inputs)(ComponentInputsWriter)) ::
     //     JField(ExternalManagementResourceKey, toJSON(h.external_management_resource)(ExResourceWriter)) ::
          JField(ExternalManagementResourceKey, toJSON(h.external_management_resource)) ::
          JField(ArtifactsKey, toJSON(h.artifacts)(ArtifactsWriter)) ::
          JField(RelatedComponentsKey, toJSON(h.related_components)) ::
          JField(OperationsKey, toJSON(h.operations)(ComponentOperationsWriter)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[Component] {
       
  //  import ComponentRequirementsSerialization.{ reader => ComponentRequirementsReader }
    import ComponentOperationsSerialization.{ reader => ComponentOperationsReader }
    import ComponentInputsSerialization.{ reader => ComponentInputsReader }
   // import ExResourceSerialization.{ reader => ExResourceReader }
    import ArtifactsSerialization.{ reader => ArtifactsReader }

    override def read(json: JValue): Result[Component] = {
      val nameField = field[String](NameKey)(json)
      val toscatypeField = field[String](ToscaTypeKey)(json)
    //  val requirementsField = field[String](RequirementsKey)(json)(ComponentRequirementsReader)
      val requirementsField = field[String](RequirementsKey)(json)
      val inputsField = field[ComponentInputs](InputsKey)(json)(ComponentInputsReader)  
    //  val externalmanagementresourceField = field[String](ExternalManagementResourceKey)(json)(ExResourceReader)
      val externalmanagementresourceField = field[String](ExternalManagementResourceKey)(json)
      val artifactsField = field[Artifacts](ArtifactsKey)(json)(ArtifactsReader)
      val relatedcomponentsField = field[String](RelatedComponentsKey)(json) 
      val operationsField = field[ComponentOperations](OperationsKey)(json)(ComponentOperationsReader)

      (nameField |@| toscatypeField |@| requirementsField |@| inputsField |@| externalmanagementresourceField |@| artifactsField |@| relatedcomponentsField |@| operationsField) {
 //       (name: String, tosca_type: String, requirements: ComponentRequirements, inputs: ComponentInputs, external_management_resource: ExResource, artifacts: Artifacts, related_components: String, operations: ComponentOperations) =>
        (name: String, tosca_type: String, requirements: String, inputs: ComponentInputs, external_management_resource: String, artifacts: Artifacts, related_components: String, operations: ComponentOperations) =>
          new Component(name, tosca_type, requirements, inputs, external_management_resource, artifacts, related_components, operations)
      }
    }
  }
}