/* 
** Copyright [2012-2013] [Megam Systems]
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
package models.json

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
import models._
import models.CloudTool

/**
 * @author ram
 *
 */
class CloudToolSerialization(charset: Charset = UTF8Charset) extends SerializationBase[CloudTool] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val NameKey = "name"
  protected val CLIKey = "cli"
  protected val CloudTemplatesKey = "cloudtemplates"

  override implicit val writer = new JSONW[CloudTool] {
    
    import CloudTemplatesSerialization.{ writer => CloudTemplatesWriter }

    override def write(h: CloudTool): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(CLIKey, toJSON(h.cli)) ::
          JField(JSONClazKey, toJSON("Megam::CloudTool")) ::
          JField(CloudTemplatesKey, toJSON(h.cloudtemplates)(CloudTemplatesWriter)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[CloudTool] {
    
        import CloudTemplatesSerialization.{ reader => CloudTemplatesReader }


    override def read(json: JValue): Result[CloudTool] = {
      val idField = field[String](IdKey)(json)
      val nameField = field[String](NameKey)(json)
      val cliField = field[String](CLIKey)(json)
      val cloudTemplateField = field[CloudTemplates](CloudTemplatesKey)(json)(CloudTemplatesReader)

      (idField |@| nameField |@| cliField |@| cloudTemplateField) {
        (id: String, name: String, cli: String, cloudtemplates) =>
          new CloudTool(id, name, cli, cloudtemplates)
      }
    }
  }
}