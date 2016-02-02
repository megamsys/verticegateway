/*
** Copyright [2013-2016] [Megam Systems]
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
package models.json.sensors

import scalaz._
import scalaz.NonEmptyList._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import models.tosca._

/**
 * @author ranjitha
 *
 */
object SensorsResultsSerialization extends io.megam.json.SerializationBase[SensorsResults] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val ResultsKey = "results"

  implicit override val writer = new JSONW[SensorsResults] {
    override def write(h: SensorsResults): JValue = {
      val nrsList: NonEmptyList[JValue] = h.map {
        nrOpt: Option[SensorsResult] =>
          (nrOpt.map { nr: SensorsResult => nr.toJValue }).getOrElse(JNothing)
      }
      JObject(JField(JSONClazKey, JString("Megam::SensorsCollection")) :: JField(ResultsKey, JArray(nrsList.list)) :: Nil)
    }
  }

  implicit override val reader = new JSONR[SensorsResults] {
    override def read(json: JValue): Result[SensorsResults] = {
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            SensorsResult.fromJValue(jValue) match {
              case Success(nr) => List(nr)
              case Failure(fail) => List[SensorsResult]()
            }
          } map { x: SensorsResult => x.some }
          val nrs: SensorsResults = list.toNel.getOrElse(nels(none))
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[SensorsResults]
      }
    }
  }
}
