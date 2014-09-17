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
import models.tosca.{ AssembliesResult, AssembliesInputs, AssembliesList }

/**
 * @author ram
 *
 */
class AssembliesResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[AssembliesResult] {

  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val NameKey = "name"
  protected val AssembliesKey = "assemblies"
  protected val InputsKey = "inputs"
  protected val CreatedAtKey ="created_at" 
    
  override implicit val writer = new JSONW[AssembliesResult] {

    import AssembliesInputsSerialization.{ writer => AssembliesInputsWriter }
    import AssembliesListSerialization.{ writer => AssembliesListWriter }

    override def write(h: AssembliesResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
        JField(JSONClazKey, toJSON("Megam::Assemblies")) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(AssembliesKey, toJSON(h.assemblies)(AssembliesListWriter)) ::
          JField(InputsKey, toJSON(h.inputs)(AssembliesInputsWriter)) :: 
          JField(CreatedAtKey, toJSON(h.created_at)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[AssembliesResult] {

    import AssembliesInputsSerialization.{ reader => AssembliesInputsReader }
    import AssembliesListSerialization.{ reader => AssembliesListReader }

    override def read(json: JValue): Result[AssembliesResult] = {
      val idField = field[String](IdKey)(json)
      val nameField = field[String](NameKey)(json)
      val assembliesField = field[AssembliesList](AssembliesKey)(json)(AssembliesListReader)
      val inputsField = field[AssembliesInputs](InputsKey)(json)(AssembliesInputsReader)  
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nameField |@| assembliesField |@| inputsField |@| createdAtField) {
        (id: String, name: String, assemblies: AssembliesList, inputs: AssembliesInputs, created_at: String) =>
          new AssembliesResult(id, name, assemblies, inputs, created_at)
      }
    }
  }
}