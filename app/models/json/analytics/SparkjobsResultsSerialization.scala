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
import models.analytics._
import models.tosca._

/**
 * @author ranjitha
 *
 */
object SparkjobsResultsSerialization extends models.json.SerializationBase[SparkjobsResults] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val ResultsKey = "results"

  implicit override val writer = new JSONW[SparkjobsResults] {
    override def write(h: SparkjobsResults): JValue = {
      val nrsList: NonEmptyList[JValue] = h.map {
        nrOpt: Option[SparkjobsResult] =>
          (nrOpt.map { nr: SparkjobsResult => nr.toJValue }).getOrElse(JNothing)
      }

      JObject(JField(JSONClazKey, JString("Megam::SparkjobsCollection")) :: JField(ResultsKey, JArray(nrsList.list)) :: Nil)
    }
  }

  implicit override val reader = new JSONR[SparkjobsResults] {
    override def read(json: JValue): Result[SparkjobsResults] = {
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            SparkjobsResult.fromJValue(jValue) match {
              case Success(nr) => List(nr)
              case Failure(fail) => List[SparkjobsResult]()
            }
          } map { x: SparkjobsResult => x.some }
          //this is screwy. Making the BalancesResults as Option[NonEmptylist[BalancesResult]] will solve it.
          val nrs: SparkjobsResults = list.toNel.getOrElse(nels(none))
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[SparkjobsResults]
      }
    }
  }
}
