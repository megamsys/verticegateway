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
package models.json.tosca.carton

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
import controllers.Constants._
import models.tosca._
import models.json.tosca._

/**
 * @author ranjitha
 *
 */
class AssemblySerialization(charset: Charset = UTF8Charset) extends io.megam.json.SerializationBase[Assembly] {

//  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val NameKey = "name"
  protected val ComponentsKey = "components"
  protected val ToscaTypeKey = "tosca_type"
  protected val PoliciesKey = "policies"
  protected val InputsKey = "inputs"
  protected val OutputsKey = "outputs"
  protected val StatusKey = "status"

  override implicit val writer = new JSONW[Assembly] {

 import ComponentsListSerialization.{ writer => ComponentsListWriter }
 import PoliciesListSerialization.{ writer => PoliciesListWriter }
 import KeyValueListSerialization.{ writer => KeyValueListWriter }
 import OperationListSerialization.{ writer => OperationListWriter }

    override def write(h: Assembly): JValue = {
      JObject(
   //     JField(JSONClazKey, toJSON("Megam::Assembly")) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(ComponentsKey, toJSON(h.components)(ComponentsListWriter)) ::
          JField(ToscaTypeKey, toJSON(h.tosca_type)) ::
          JField(PoliciesKey, toJSON(h.policies)(PoliciesListWriter)) ::
          JField(InputsKey, toJSON(h.inputs)(KeyValueListWriter)) ::
          JField(OutputsKey, toJSON(h.outputs)(KeyValueListWriter)) ::
          JField(StatusKey, toJSON(h.status)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[Assembly] {

     import ComponentsListSerialization.{ reader => ComponentsListReader }
     import PoliciesListSerialization.{reader => PoliciesListReader }
     import KeyValueListSerialization.{reader => KeyValueListReader }
     import OperationListSerialization.{reader => OperationListReader }

    override def read(json: JValue): Result[Assembly] = {
      val nameField = field[String](NameKey)(json)
      val componentsField = field[ComponentsList](ComponentsKey)(json)(ComponentsListReader)
      val toscatypeField = field[String](ToscaTypeKey)(json)
      val policiesField = field[PoliciesList](PoliciesKey)(json)(PoliciesListReader)
      val inputsField = field[KeyValueList](InputsKey)(json)(KeyValueListReader)
      val outputsField = field[KeyValueList](OutputsKey)(json)(KeyValueListReader)
      val statusField = field[String](StatusKey)(json)

      (nameField |@| componentsField |@| toscatypeField |@| policiesField |@| inputsField |@| outputsField |@| statusField) {
          (name: String, components: ComponentsList, tosca_type: String, policies: PoliciesList, inputs: KeyValueList, outputs: KeyValueList, status: String) =>
          new Assembly(name, components, tosca_type, policies, inputs, outputs, status)
      }
    }
  }
}
