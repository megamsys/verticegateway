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
package models.json.tosca

import scalaz._
import scalaz.NonEmptyList._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import controllers.funnel.SerializationBase
import models.tosca._
import java.nio.charset.Charset
/**
 * @author rajthilak
 *
 */
object MetricListSerialization extends SerializationBase[MetricList] {

  implicit override val writer = new JSONW[MetricList] {
    override def write(h: MetricList): JValue = {
      val nrsList: Option[List[JValue]] = h.map {
        nrOpt: Metric => nrOpt.toJValue
      }.some

      JArray(nrsList.getOrElse(List.empty[JValue]))
    }
  }

  implicit override val reader = new JSONR[MetricList] {
    override def read(json: JValue): Result[MetricList] = {
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            Metric.fromJValue(jValue) match {
              case Success(nr) => List(nr)
              case Failure(fail) => List[Metric]()
            }
          }.some

          val nrs: MetricList = MetricList(list.getOrElse(MetricList.empty))
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[MetricList]
      }
    }
  }
}
