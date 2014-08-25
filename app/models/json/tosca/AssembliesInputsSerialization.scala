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
import models.tosca.{ AssembliesResult, AssembliesInputs}
import org.megam.common.enumeration._


/**
 * @author ram
 *
 */
object AssembliesInputsSerialization extends SerializationBase[AssembliesInputs] {
  protected val IdKey = "id"
  protected val AssembliesTypeKey = "assemblies_type"
  protected val LabelKey = "label"
  

  override implicit val writer = new JSONW[AssembliesInputs] {


    override def write(h: AssembliesInputs): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
        JField(AssembliesTypeKey, toJSON(h.assemblies_type)) ::
        JField(LabelKey, toJSON(h.label))  ::Nil)
    }
  }

  override implicit val reader = new JSONR[AssembliesInputs] {

    override def read(json: JValue): Result[AssembliesInputs] = {
      val idField = field[String](IdKey)(json)
      val assembliestypeField = field[String](AssembliesTypeKey)(json)
      val labelField = field[String](LabelKey)(json)
    
      (idField |@| assembliestypeField |@| labelField) {
        (id: String, assemblies_type: String, label: String) =>
          new AssembliesInputs(id, assemblies_type, label)
      }
    }
  }
}