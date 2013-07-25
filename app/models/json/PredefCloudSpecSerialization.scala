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
import models.{ PredefCloudSpec }

/**
 * @author ram
 *
 */
object PredefCloudSpecSerialization extends SerializationBase[PredefCloudSpec] {

  protected val NameKey = "type_name"
  protected val GroupsKey = "groups"
  protected val ImageKey = "image"
  protected val FlavorKey = "flavor"

  override implicit val writer = new JSONW[PredefCloudSpec] {

    override def write(h: PredefCloudSpec): JValue = {
      JObject(
        JField(NameKey, toJSON(h.type_name)) ::
          JField(GroupsKey, toJSON(h.groups)) ::
          JField(ImageKey, toJSON(h.image)) ::
          JField(FlavorKey, toJSON(h.flavor)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[PredefCloudSpec] {

    override def read(json: JValue): Result[PredefCloudSpec] = {
      val typeNameField = field[String](NameKey)(json)
      val groupField = field[String](GroupsKey)(json)
      val imageField = field[String](ImageKey)(json)
      val flavorField = field[String](FlavorKey)(json)
      

      (typeNameField |@| groupField |@| imageField |@| flavorField) {
        (typeName: String, groups: String, image: String, flavor: String) =>
          new PredefCloudSpec(typeName, groups, image, flavor)
      }
    }
  }
}