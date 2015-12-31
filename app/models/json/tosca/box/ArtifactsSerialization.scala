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
import models.tosca.{ Artifacts, KeyValueList }

/**
 * @author rajthilak
 *
 */

object ArtifactsSerialization extends models.json.SerializationBase[Artifacts] {

  protected val ArtifactTypeKey = "artifact_type"
  protected val ContentKey = "content"
  protected val RequirementsKey = "requirements"

  override implicit val writer = new JSONW[Artifacts] {

    import models.json.tosca.KeyValueListSerialization.{ writer => KeyValueListWriter }

    override def write(h: Artifacts): JValue = {
      JObject(
        JField(ArtifactTypeKey, toJSON(h.artifact_type)) ::
          JField(ContentKey, toJSON(h.content)) ::
          JField(RequirementsKey, toJSON(h.requirements)(KeyValueListWriter)) ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[Artifacts] {

    import models.json.tosca.KeyValueListSerialization.{ reader => KeyValueListReader }

    override def read(json: JValue): Result[Artifacts] = {
      val artifacttypeField = field[String](ArtifactTypeKey)(json)
      val contentField = field[String](ContentKey)(json)
      val requirementsField = field[KeyValueList](RequirementsKey)(json)(KeyValueListReader)

      (artifacttypeField |@| contentField |@| requirementsField) {
        (artifacttype: String, content: String, requirements: KeyValueList) =>
          new Artifacts(artifacttype, content, requirements)
      }
    }
  }
}
