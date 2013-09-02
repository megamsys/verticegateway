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

/**
 * @author ram
 *
 */
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
import models.{ NodeCommand, NodeSystemProvider, NodeCompute, NodeCloudToolService }
import models.NodeSystemProvider

/**
 * @author ram
 *
 */
object NodeCommandSerialization extends SerializationBase[NodeCommand] {
  protected val SysProviderKey = "systemprovider"
  protected val ComputeKey = "compute"
  protected val CloudToolKey = "cloudtool"

  override implicit val writer = new JSONW[NodeCommand] {

    import NodeSystemProviderSerialization.{ writer => NodeSystemProvWriter }
    import NodeComputeSerialization.{ writer => NodeComputeWriter }
    import NodeCloudToolServiceSerialization.{ writer => NodeCloudToolWriter }

    override def write(h: NodeCommand): JValue = {
      JObject(
        JField(SysProviderKey, toJSON(h.systemprovider)(NodeSystemProvWriter)) ::
          JField(ComputeKey, toJSON(h.compute)(NodeComputeWriter)) ::
          JField(CloudToolKey, toJSON(h.cloudtool)(NodeCloudToolWriter)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[NodeCommand] {

    import NodeSystemProviderSerialization.{ reader => NodeSystemProviderReader }
    import NodeComputeSerialization.{ reader => NodeComputeReader }
    import NodeCloudToolServiceSerialization.{ reader => NodeCloudToolServiceReader }

    override def read(json: JValue): Result[NodeCommand] = {
      val sysField = field[NodeSystemProvider](SysProviderKey)(json)(NodeSystemProviderReader)
      val compField = field[NodeCompute](ComputeKey)(json)(NodeComputeReader)
      val cloudtoolField = field[NodeCloudToolService](CloudToolKey)(json)(NodeCloudToolServiceReader)
      (sysField |@| compField |@| cloudtoolField) {
        (sysField: NodeSystemProvider, compField: NodeCompute, cloudtools: NodeCloudToolService) =>
          new NodeCommand(sysField, compField, cloudtools)
      }

    }
  }
}