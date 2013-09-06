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
import models.{ NodeCloudToolService, NodeCloudToolChef }

/**
 * @author ram
 *
 */
object NodeCloudToolServiceSerialization extends SerializationBase[NodeCloudToolService] {
  protected val ChefKey = "chef"

  override implicit val writer = new JSONW[NodeCloudToolService] {

    import NodeCloudToolChefSerialization.{ writer => NodeCloudToolChefWriter }

    override def write(h: NodeCloudToolService): JValue = {
      JObject(
        JField(ChefKey, toJSON(h.chef)(NodeCloudToolChefWriter)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[NodeCloudToolService] {

    import NodeCloudToolChefSerialization.{ reader => NodeCloudToolChefReader }

    override def read(json: JValue): Result[NodeCloudToolService] = {
      val chefValSF = field[NodeCloudToolChef](ChefKey)(json)(NodeCloudToolChefReader)
      chefValSF map {chefVal => new NodeCloudToolService(chefVal) }
    }
  }
}