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
import models.tosca.{ ArtifactRequirements }

/**
 * @author rajthilak
 *
 */
/*
object ArtifactRequirementsSerialization extends SerializationBase[ArtifactRequirements] {

  protected val RequirementTypeKey = "feature1"

  override implicit val writer = new JSONW[ArtifactRequirements] {

    override def write(h: ArtifactRequirements): JValue = {
      JObject(
        JField(RequirementTypeKey, toJSON(h.requirement_type)) ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[ArtifactRequirements] {

    override def read(json: JValue): Result[ArtifactRequirements] = {
      val requirementtypeField = field[String](RequirementTypeKey)(json)
      
      (requirementtypeField) {
        (requirement_type: String) =>
          new ArtifactRequirements(requirement_type)
      }
    }
  }
}

*/