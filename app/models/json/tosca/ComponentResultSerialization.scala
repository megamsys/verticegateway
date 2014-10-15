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
import models.tosca.{ ComponentResult, ComponentsList, ComponentInputs, ComponentOperations, Artifacts, ComponentRequirements }

/**
 * @author rajthilak
 *
 */
class ComponentResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[ComponentResult] {

  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val NameKey = "name"
  protected val ToscaTypeKey = "tosca_type"
  protected val RequirementsKey = "requirements"
  protected val InputsKey = "inputs"
  protected val ExternalManagementResourceKey = "external_management_resource"
  protected val ArtifactsKey = "artifacts"
  protected val RelatedComponentsKey ="related_components"
  protected val OperationsKey = "operations"
  protected val CreatedAtKey ="created_at" 
    
  override implicit val writer = new JSONW[ComponentResult] {

    import ComponentRequirementsSerialization.{ writer => ComponentRequirementsWriter }
    import models.json.tosca.ComponentInputsSerialization.{ writer => ComponentInputsWriter }
    import models.json.tosca.ArtifactsSerialization.{ writer => ArtifactsWriter }
    import models.json.tosca.ComponentOperationsSerialization.{ writer => ComponentOperationsWriter }
    
    override def write(h: ComponentResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(JSONClazKey, toJSON("Megam::Components")) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(ToscaTypeKey, toJSON(h.tosca_type)) ::
          JField(RequirementsKey, toJSON(h.requirements)(ComponentRequirementsWriter)) ::
          JField(InputsKey, toJSON(h.inputs)(ComponentInputsWriter)) ::
          JField(ExternalManagementResourceKey, toJSON(h.external_management_resource)) ::
          JField(ArtifactsKey, toJSON(h.artifacts)(ArtifactsWriter)) ::
          JField(RelatedComponentsKey, toJSON(h.related_components)) ::
          JField(OperationsKey, toJSON(h.operations)(ComponentOperationsWriter)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[ComponentResult] {
    
    import models.json.tosca.ComponentInputsSerialization.{ reader => ComponentInputsReader}
    import models.json.tosca.ArtifactsSerialization.{ reader => ArtifactsReader }
    import models.json.tosca.ComponentOperationsSerialization.{ reader => ComponentOperationsReader }
    import ComponentRequirementsSerialization.{ reader => ComponentRequirementsReader }

    override def read(json: JValue): Result[ComponentResult] = {
      val idField = field[String](IdKey)(json)
      val nameField = field[String](NameKey)(json)
      val toscaTypeField = field[String](ToscaTypeKey)(json)
      val requirementsField = field[ComponentRequirements](RequirementsKey)(json)(ComponentRequirementsReader)
      val inputsField = field[ComponentInputs](InputsKey)(json)(ComponentInputsReader)
      val externalManagementResourceField = field[String](ExternalManagementResourceKey)(json)  
      val artifactsField = field[Artifacts](ArtifactsKey)(json)(ArtifactsReader)
      val relatedComponentsField = field[String](RelatedComponentsKey)(json)
      val operationsField = field[ComponentOperations](OperationsKey)(json)(ComponentOperationsReader)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nameField |@| toscaTypeField |@| requirementsField |@| inputsField |@| externalManagementResourceField |@| artifactsField |@| relatedComponentsField |@| operationsField |@| createdAtField) {
          (id: String, name: String, tosca_type: String, requirements: ComponentRequirements, inputs: ComponentInputs, external_management_resource: String, artifacts: Artifacts, related_components: String, operations: ComponentOperations, created_at: String) =>
          new ComponentResult(id, name, tosca_type, requirements, inputs, external_management_resource, artifacts, related_components, operations, created_at)
      }
    }
  }
}