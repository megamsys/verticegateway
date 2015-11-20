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
package models.json.tosca.box

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
import models.json.tosca._
import models.tosca.{ Component, Artifacts, KeyValueList, OperationList, BindLinks, Repo }

/**
 * @author ranjitha
 *
 */
class ComponentSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[Component] {

  protected val NameKey = "name"
  protected val ToscaTypeKey = "tosca_type"
  protected val InputsKey = "inputs"
  protected val OutputsKey = "outputs"
  protected val EnvsKey = "envs"
  protected val ArtifactsKey = "artifacts"
  protected val RelatedComponentsKey = "related_components"
  protected val OperationsKey = "operations"
  protected val StatusKey = "status"
  protected val RepoKey = "repo"

  override implicit val writer = new JSONW[Component] {

    import models.json.tosca.box.ArtifactsSerialization.{ writer => ArtifactsWriter }
    import models.json.tosca.KeyValueListSerialization.{ writer => KeyValueListWriter }
    import models.json.tosca.OperationListSerialization.{ writer => OperationListWriter }
    import models.json.tosca.box.RepoSerialization.{ writer => RepoWriter }
    import models.json.tosca.box.BindLinksSerialization.{ writer => BindLinksWriter }


    override def write(h: Component): JValue = {
      JObject(
        JField(NameKey, toJSON(h.name)) ::
          JField(ToscaTypeKey, toJSON(h.tosca_type)) ::
          JField(InputsKey, toJSON(h.inputs)(KeyValueListWriter)) ::
          JField(OutputsKey, toJSON(h.outputs)(KeyValueListWriter)) ::
          JField(EnvsKey, toJSON(h.envs)(KeyValueListWriter)) ::
          JField(ArtifactsKey, toJSON(h.artifacts)(ArtifactsWriter)) ::
          JField(RelatedComponentsKey, toJSON(h.related_components)(BindLinksWriter)) ::
          JField(OperationsKey, toJSON(h.operations)(OperationListWriter)) ::
          JField(RepoKey, toJSON(h.repo)(RepoWriter)) ::
          JField(StatusKey, toJSON(h.status)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[Component] {

    import KeyValueListSerialization.{ reader => KeyValueListReader }
    import ArtifactsSerialization.{ reader => ArtifactsReader }
    import OperationListSerialization.{ reader => OperationListReader }
    import RepoSerialization.{ reader => RepoReader }
    import BindLinksSerialization.{ reader => BindLinksReader }

    override def read(json: JValue): Result[Component] = {
      val nameField = field[String](NameKey)(json)
      val toscatypeField = field[String](ToscaTypeKey)(json)
      val inputsField = field[KeyValueList](InputsKey)(json)(KeyValueListReader)
      val outputsField = field[KeyValueList](OutputsKey)(json)(KeyValueListReader)
      val envsField = field[KeyValueList](EnvsKey)(json)(KeyValueListReader)
      val artifactsField = field[Artifacts](ArtifactsKey)(json)(ArtifactsReader)
      val relatedcomponentsField = field[BindLinks](RelatedComponentsKey)(json)(BindLinksReader)
      val operationsField = field[OperationList](OperationsKey)(json)(OperationListReader)
      val statusField = field[String](StatusKey)(json)
      val repoField = field[Repo](RepoKey)(json)(RepoReader)

      (nameField |@| toscatypeField |@| inputsField |@| outputsField |@| envsField |@| artifactsField |@| relatedcomponentsField |@| operationsField |@| repoField |@| statusField) {
        (name: String, tosca_type: String, inputs: KeyValueList, outputs: KeyValueList, envs: KeyValueList, artifacts: Artifacts, related_components: BindLinks, operations: OperationList, repo: Repo, status: String) =>
          new Component(name, tosca_type, inputs, outputs, envs, artifacts, related_components, operations, repo, status)
      }
    }
  }
}
