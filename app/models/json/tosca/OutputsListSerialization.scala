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
object OutputsListSerialization extends SerializationBase[OutputsList] {

  implicit override val writer = new JSONW[OutputsList] {
    override def write(h: OutputsList): JValue = {
      val nrsList: Option[List[JValue]] = h.map {
        nrOpt: Output => nrOpt.toJValue
      }.some
      
      JArray(nrsList.getOrElse(List.empty[JValue]))
    }
  }

  implicit override val reader = new JSONR[OutputsList] {
    override def read(json: JValue): Result[OutputsList] = {
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            Output.fromJValue(jValue) match {
              case Success(nr) => List(nr)
              case Failure(fail) => List[Output]()
            }
          }.some

          val nrs: OutputsList = OutputsList(list.getOrElse(OutputsList.empty))
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[OutputsList]
      }
    }
  }
}