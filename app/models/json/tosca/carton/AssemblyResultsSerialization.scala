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
package models.json.tosca.carton


import scalaz._
import scalaz.NonEmptyList._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import models.tosca._

/**
 * @author rajthilak
 *
 */
object AssemblyResultsSerialization extends io.megam.json.SerializationBase[AssemblyResults] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val ResultsKey = "results"

  implicit override val writer = new JSONW[AssemblyResults] {
    override def write(h: AssemblyResults): JValue = {
      val nrsList: NonEmptyList[JValue] = h.map {
        nrOpt: Option[AssemblyResult] =>
            (nrOpt.map { nr: AssemblyResult => nr.toJValue }).getOrElse(JNothing)
      }
      JObject(JField(JSONClazKey,JString("Megam::AssemblyCollection")) :: JField(ResultsKey,JArray(nrsList.list)) :: Nil)
    }
  }

  implicit override val reader = new JSONR[AssemblyResults] {
    override def read(json: JValue): Result[AssemblyResults] = {
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            AssemblyResult.fromJValue(jValue) match {
              case Success(nr)   => List(nr)
              case Failure(fail) => List[AssemblyResult]()
            }
          } map { x: AssemblyResult => x.some }
          //this is screwy. Making the NodeResults as Option[NonEmptylist[AssembliesResult]] will solve it.
          val nrs: AssemblyResults = list.toNel.getOrElse(nels(none))
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[AssemblyResults]
      }
    }
  }
}
