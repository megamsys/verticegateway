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
object PredefResultsSerialization extends SerializationBase[PredefResults] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ

  implicit override val writer = new JSONW[PredefResults] {
    override def write(h: PredefResults): JValue = {
      val nrsList: NonEmptyList[JValue] = h.map {
        nrOpt: Option[PredefResult] =>
          (nrOpt.map { nr: PredefResult => nr.toJValue }).getOrElse(JNothing)
      }

      JArray((JField(JSONClazKey, toJSON("Megam::PredefCollection")) +:nrsList.list))
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
      PredefResult already has an implicit reader, hence use it.
       */
  implicit override val reader = new JSONR[PredefResults] {
    override def read(json: JValue): Result[PredefResults] = {
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            PredefResult.fromJValue(jValue) match {
              case Success(nr)   => List(nr)
              case Failure(fail) => List[PredefResult]()
            }
          } map { x: PredefResult => x.some }
          //this is screwy. Making the PredefResults as Option[NonEmptylist[NodeResult]] will solve it.
          val nrs: PredefResults = list.toNel.getOrElse(nels(none))
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failNel[PredefResults]
      }
    }
  }
}
