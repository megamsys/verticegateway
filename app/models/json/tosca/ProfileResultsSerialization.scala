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
package models.json.tosca


import scalaz._
import scalaz.NonEmptyList._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import controllers.funnel.SerializationBase
import models._
import models.tosca._


/**
 * @author morpheyesh
 *
 */
object ProfileResultsSerialization extends SerializationBase[ProfileResults] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val ResultsKey = "results"

  implicit override val writer = new JSONW[ProfileResults] {
    override def write(h: ProfileResults): JValue = {
      val nrsList: NonEmptyList[JValue] = h.map {
        nrOpt: Option[ProfileResult] =>
          (nrOpt.map { nr: ProfileResult => nr.toJValue }).getOrElse(JNothing)
      }

      JObject(JField(JSONClazKey, JString("Megam::ProfileCollection")) :: JField(ResultsKey, JArray(nrsList.list)) :: Nil)
    }
  }

 
  implicit override val reader = new JSONR[ProfileResults] {
    override def read(json: JValue): Result[ProfileResults] = {
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            ProfileResult.fromJValue(jValue) match {
              case Success(nr)   => List(nr)
              case Failure(fail) => List[ProfileResult]()
            }
          } map { x: ProfileResult => x.some }
          //this is screwy. Making the ProfileResults as Option[NonEmptylist[ProfileResult]] will solve it.
          val nrs: ProfileResults = list.toNel.getOrElse(nels(none))
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[ProfileResults]
      }
    }
  }
}
