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
object PayloadListSerialization extends SerializationBase[PayloadList] {

  implicit override val writer = new JSONW[PayloadList] {
    override def write(h: PayloadList): JValue = {
      val nrsList: Option[List[JValue]] = h.map {
        nrOpt: Payload => nrOpt.toJValue
      }.some

      JArray(nrsList.getOrElse(List.empty[JValue]))
    }
  }

  implicit override val reader = new JSONR[PayloadList] {
    override def read(json: JValue): Result[PayloadList] = {
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            Payload.fromJValue(jValue) match {
              case Success(nr) => List(nr)
              case Failure(fail) => List[Payload]()
            }
          }.some

          val nrs: PayloadList = PayloadList(list.getOrElse(PayloadList.empty))
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[PayloadList]
      }
    }
  }
}
