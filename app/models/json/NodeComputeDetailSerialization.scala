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
import models.{ NodeComputeDetail }

/**
 * @author ram
 *
 */
object NodeComputeDetailSerialization extends SerializationBase[NodeComputeDetail] {

  protected val ImageKey = "image"
  protected val FlavorKey = "flavor"

  override implicit val writer = new JSONW[NodeComputeDetail] {

    override def write(h: NodeComputeDetail): JValue = {
      JObject(
          JField(ImageKey, toJSON(h.image)) ::
          JField(FlavorKey, toJSON(h.flavor)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[NodeComputeDetail] {

    override def read(json: JValue): Result[NodeComputeDetail] = {
      val imageField = field[String](ImageKey)(json)
      val flavorField = field[String](FlavorKey)(json)
      

      (imageField |@| flavorField) {
        (image: String, flavor: String) =>
          new NodeComputeDetail(image, flavor)
      }
    }
  }
}