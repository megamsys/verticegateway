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
import models._
import models.{ CloudTemplate, CloudInstructionGroup }

/**
 * @author ram
 *
 */
class CloudTemplateSerialization(charset: Charset = UTF8Charset) extends SerializationBase[CloudTemplate] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val CCKey = "cctype"
  protected val CDIKey = "cloud_instruction_group"

  import models.json.CloudInstructionGroupSerialization

  override implicit val writer = new JSONW[CloudTemplate] {

    override def write(h: CloudTemplate): JValue = {
      val cig = new CloudInstructionGroupSerialization()
      JObject(
        JField(CCKey, toJSON(h.cctype)) ::
          JField(JSONClazKey, toJSON("Megam::CloudTemplate")) ::
          JField(CDIKey, toJSON(h.cloud_instruction_group)(cig.writer)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[CloudTemplate] {

    override def read(json: JValue): Result[CloudTemplate] = {
      val cig = new CloudInstructionGroupSerialization()
      val ccField = field[String](CCKey)(json)
      val cdiField = field[CloudInstructionGroup](CDIKey)(json)(cig.reader)

      (ccField |@| cdiField) {
        (cc_type: String, cloud_instruction_group: CloudInstructionGroup) =>
          new CloudTemplate(cc_type, cloud_instruction_group)
      }
    }
  }
}