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
import controllers.funnel.FunnelErrors._
import controllers.Constants._
import models.tosca._
import models.json.tosca._
/**
 * @author ram
 *
 */
class AssembliesResultSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[AssembliesResult] {

  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val AccountIdKey = "accounts_id"
  protected val NameKey = "name"
  protected val AssembliesKey = "assemblies"
  protected val InputsKey = "inputs"
  protected val CreatedAtKey ="created_at"

  override implicit val writer = new JSONW[AssembliesResult] {

    import KeyValueListSerialization.{ writer => KeyValueListWriter }
    import AssemblyLinksSerialization.{ writer => AssemblyLinksWriter }


    override def write(h: AssembliesResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
        JField(AccountIdKey, toJSON(h.accounts_id)) ::
        JField(JSONClazKey, toJSON("Megam::Assemblies")) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(AssembliesKey, toJSON(h.assemblies)(AssemblyLinksWriter)) ::
          JField(InputsKey, toJSON(h.inputs)(KeyValueListWriter)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[AssembliesResult] {

    import KeyValueListSerialization.{ reader => KeyValueListReader }
    import AssemblyLinksSerialization.{ reader => AssemblyLinksReader }


    override def read(json: JValue): Result[AssembliesResult] = {
      val idField = field[String](IdKey)(json)
      val accountIdField = field[String](AccountIdKey)(json)
      val nameField = field[String](NameKey)(json)
      val assembliesField = field[AssemblyLinks](AssembliesKey)(json)(AssemblyLinksReader)
      val inputsField = field[KeyValueList](InputsKey)(json)(KeyValueListReader)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| accountIdField |@| nameField |@| assembliesField |@| inputsField |@| createdAtField) {
        (id: String, accountId: String, name: String, assemblies: AssemblyLinks, inputs: KeyValueList, created_at: String) =>
          new AssembliesResult(id, accountId, name, assemblies, inputs, created_at)
      }
    }
  }
}
