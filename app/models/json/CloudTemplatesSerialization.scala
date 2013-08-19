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
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import controllers.funnel.SerializationBase
import models._

/**
 * @author ram
 *
 */
object CloudTemplatesSerialization extends SerializationBase[CloudTemplates] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val ResultsKey = "results"

  implicit override val writer = new JSONW[CloudTemplates] {
    override def write(h: CloudTemplates): JValue = {

      val nrsList: List[JValue] = h.map { nr: CloudTemplate => (nr.toJValue.some.getOrElse(JNothing)) }
      JObject(JField(JSONClazKey, JString("Megam::CloudTemplateCollection"))
        :: JField(ResultsKey, JArray(nrsList)) :: Nil)

    }

  }

  /* Read - JArray(List[NodeResult]) which translates to :
        JArray(List(
          JObject(
            List(
              JField(name,JInt(code)),
              JField(value,JString(msg))
              .....
            )
          )
        )
      )
      NodeResult already has an implicit reader, hence use it.
       */
  implicit override val reader = new JSONR[CloudTemplates] {
    override def read(json: JValue): Result[CloudTemplates] = {
      play.api.Logger.debug("CloudTemplatesSerialization: ==> jObjectList" + json)

      json match {
        case JArray(jObjectList) => {
          play.api.Logger.debug("CloudTemplatesSerialization: ==> jObjectList" + jObjectList)
          val list = jObjectList.flatMap { jValue: JValue =>
            CloudTemplate.fromJValue(jValue) match {
              case Success(nr)   => List(nr)
              case Failure(fail) => List[CloudTemplate]()
            }
          }
          val nrs: CloudTemplates = list.some.getOrElse(CloudTemplates.empty)
          nrs.successNel[Error]
        }
        case j => {
          UnexpectedJSONError(j, classOf[JArray]).failNel[CloudTemplates]
        }
      }
    }
  }
}