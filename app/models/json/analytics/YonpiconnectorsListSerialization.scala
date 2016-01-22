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
package models.json.analytics

import scalaz._
import scalaz.NonEmptyList._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import models.tosca._
import java.nio.charset.Charset
import models.analytics.{YonpiconnectorsList, Yonpiconnectors}

/**
 * @author ranjitha
 *
 */
object YonpiconnectorsListSerialization extends models.json.SerializationBase[YonpiconnectorsList] {

  implicit override val writer = new JSONW[YonpiconnectorsList] {
    override def write(h: YonpiconnectorsList): JValue = {
      val nrsList: Option[List[JValue]] = h.map {
        nrOpt: Yonpiconnectors => nrOpt.toJValue
      }.some

      JArray(nrsList.getOrElse(List.empty[JValue]))
    }
  }

  implicit override val reader = new JSONR[YonpiconnectorsList] {
    override def read(json: JValue): Result[YonpiconnectorsList] = {
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            Yonpiconnectors.fromJValue(jValue) match {
              case Success(nr) => List(nr)
              case Failure(fail) => List[Yonpiconnectors]()
            }
          }.some

          val nrs: YonpiconnectorsList = YonpiconnectorsList(list.getOrElse(YonpiconnectorsList.empty))
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[YonpiconnectorsList]
      }
    }
  }
}
