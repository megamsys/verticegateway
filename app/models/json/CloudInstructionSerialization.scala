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
import models.CloudInstruction

/**
 * @author ram
 *
 */
class CloudInstructionSerialization(charset: Charset = UTF8Charset) extends SerializationBase[CloudInstruction] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val ActionKey = "action"
  protected val CommandKey = "command"
  protected val NameKey = "name"

  override implicit val writer = new JSONW[CloudInstruction] {

    override def write(h: CloudInstruction): JValue = {
      JObject(
        JField(ActionKey, toJSON(h.action)) ::
          JField(CommandKey, toJSON(h.command)) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(JSONClazKey, toJSON("Megam::CloudInstruction")) :: Nil)
    }
  }

  override implicit val reader = new JSONR[CloudInstruction] {

    override def read(json: JValue): Result[CloudInstruction] = {
      val actionField = field[String](ActionKey)(json)
      val commandField = field[String](CommandKey)(json)
      val nameField = field[String](NameKey)(json)

      (actionField |@| commandField |@| nameField) {
        (action: String, command: String, name: String) =>
          new CloudInstruction(action,command,name)
      }
    }
  }
}