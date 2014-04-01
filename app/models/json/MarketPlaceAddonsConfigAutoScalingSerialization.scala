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
import models.{ MarketPlaceAddonsConfigAutoScaling }
/**
 * @author rajthilak
 *
 */
object MarketPlaceAddonsConfigAutoScalingSerialization  extends SerializationBase[MarketPlaceAddonsConfigAutoScaling] {
  protected val CPUThresholdKey = "cputhreshold"
  protected val MemoryThresholdKey = "memorythreshold"  
  protected val NoofInstances = "noofinstances"

  override implicit val writer = new JSONW[MarketPlaceAddonsConfigAutoScaling] {

    override def write(h: MarketPlaceAddonsConfigAutoScaling): JValue = {
      JObject(
        JField(CPUThresholdKey, toJSON(h.cputhreshold)) ::
          JField(MemoryThresholdKey, toJSON(h.memorythreshold)) ::
          JField(NoofInstances, toJSON(h.noofinstances)) ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[MarketPlaceAddonsConfigAutoScaling] {

    override def read(json: JValue): Result[MarketPlaceAddonsConfigAutoScaling] = {
      val cpuThresholdField = field[String](CPUThresholdKey)(json)
      val memoryThresholdField = field[String](MemoryThresholdKey)(json)      
      val noofInstancesField = field[String](NoofInstances)(json)
      
      (cpuThresholdField |@| memoryThresholdField |@| noofInstancesField ) {
        (cputhreshold: String, memorythreshold: String, noofinstances) =>
          new MarketPlaceAddonsConfigAutoScaling(cputhreshold, memorythreshold, noofinstances)
      }
    }
  }
}