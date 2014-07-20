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
import models.{ NodeCompute, NodeComputeDetail, NodeComputeAccess }

/**
 * @author ram
 *
 */
object NodeComputeSerialization extends SerializationBase[NodeCompute] {

  protected val CCTypeKey = "cctype"
  protected val CCKey = "cc"
  protected val CAKey = "access"

  override implicit val writer = new JSONW[NodeCompute] {

    import models.json.NodeComputeDetailSerialization.{ writer => NodeComputeDetailWriter }
    import models.json.NodeComputeAccessSerialization.{ writer => NodeComputeAccessWriter }

    override def write(h: NodeCompute): JValue = {
      JObject(
        JField(CCTypeKey, toJSON(h.cctype)) ::
          JField(CCKey, toJSON(h.cc)(NodeComputeDetailWriter)) ::
          JField(CAKey, toJSON(h.access)(NodeComputeAccessWriter)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[NodeCompute] {

    import models.json.NodeComputeDetailSerialization.{ reader => NodeComputeDetailReader }
    import models.json.NodeComputeAccessSerialization.{ reader => NodeComputeAccessReader }

    override def read(json: JValue): Result[NodeCompute] = {
      val cctypeField = field[String](CCTypeKey)(json)
      val ccField = field[NodeComputeDetail](CCKey)(json)(NodeComputeDetailReader)
      val caField = field[NodeComputeAccess](CAKey)(json)(NodeComputeAccessReader)

      (cctypeField |@| ccField |@| caField) {
        (cctypeField: String, ccField: NodeComputeDetail, caField: NodeComputeAccess) =>
          new NodeCompute(cctypeField, ccField, caField)
      }
    }
  }
}