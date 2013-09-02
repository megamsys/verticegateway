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
import models.{ NodeCloudToolChef}

/**
 * @author ram
 *
 */
object NodeCloudToolChefSerialization extends SerializationBase[NodeCloudToolChef] {
  protected val CommandKey = "command"
  protected val PluginKey = "plugin"
  protected val RunlistKey = "run_list"
  protected val NameKey = "name"

  override implicit val writer = new JSONW[NodeCloudToolChef] {

    override def write(h: NodeCloudToolChef): JValue = {
      JObject(
        JField(CommandKey, toJSON(h.command)) ::
          JField(PluginKey, toJSON(h.plugin)) ::
          JField(RunlistKey, toJSON(h.run_list)) ::
          JField(NameKey, toJSON(h.name)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[NodeCloudToolChef] {

    override def read(json: JValue): Result[NodeCloudToolChef] = {
      val commandField = field[String](CommandKey)(json)
      val pluginField = field[String](PluginKey)(json)
      val runlistField = field[String](RunlistKey)(json)
      val nameField = field[String](NameKey)(json)

      (commandField |@| pluginField |@| runlistField |@| nameField) {
        (command: String, plugin: String, run_list: String, name: String) =>
          new NodeCloudToolChef(command,plugin,run_list,name)
      }
    }
  }
}