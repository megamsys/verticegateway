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
package models.json.team


import scalaz._
import scalaz.NonEmptyList._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import models.base._
import models.team._


/**
 * @author morpheyesh
 *
 */
object OrganizationsResultsSerialization extends io.megam.json.SerializationBase[OrganizationsResults] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val ResultsKey = "results"

  implicit override val writer = new JSONW[OrganizationsResults] {
    override def write(h: OrganizationsResults): JValue = {
      val nrsList: NonEmptyList[JValue] = h.map {
        nrOpt: Option[OrganizationsResult] =>
          (nrOpt.map { nr: OrganizationsResult => nr.toJValue }).getOrElse(JNothing)
      }

      JObject(JField(JSONClazKey, JString("Megam::OrganizationsCollection")) :: JField(ResultsKey, JArray(nrsList.list)) :: Nil)
    }
  }


  implicit override val reader = new JSONR[OrganizationsResults] {
    override def read(json: JValue): Result[OrganizationsResults] = {
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            OrganizationsResult.fromJValue(jValue) match {
              case Success(nr)   => List(nr)
              case Failure(fail) => List[OrganizationsResult]()
            }
          } map { x: OrganizationsResult => x.some }
          //this is screwy. Making the OrganizationsResults as Option[NonEmptylist[OrganizationsResult]] will solve it.
          val nrs: OrganizationsResults = list.toNel.getOrElse(nels(none))
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[OrganizationsResults]
      }
    }
  }
}
