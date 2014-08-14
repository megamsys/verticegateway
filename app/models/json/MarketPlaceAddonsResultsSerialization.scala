/* 
** Copyright [2013-2014] [Megam Systems]
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
 * @author rajthilak
 *
 */
object MarketPlaceAddonsResultsSerialization extends SerializationBase[MarketPlaceAddonsResults] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val ResultsKey = "results"

  implicit override val writer = new JSONW[MarketPlaceAddonsResults] {
    override def write(h: MarketPlaceAddonsResults): JValue = {
      val nrsList: NonEmptyList[JValue] = h.map {
        nrOpt: Option[MarketPlaceAddonsResult] =>
          (nrOpt.map { nr: MarketPlaceAddonsResult => nr.toJValue }).getOrElse(JNothing)
      }
      JObject(JField(JSONClazKey, JString("Megam::MarketPlaceAddonsCollection")) :: JField(ResultsKey, JArray(nrsList.list)) :: Nil)
    }
  }

  
  implicit override val reader = new JSONR[MarketPlaceAddonsResults] {
    override def read(json: JValue): Result[MarketPlaceAddonsResults] = {
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            MarketPlaceAddonsResult.fromJValue(jValue) match {
              case Success(nr)   => List(nr)
              case Failure(fail) => List[MarketPlaceAddonsResult]()
            }
          } map { x: MarketPlaceAddonsResult => x.some }
          //this is screwy. Making the RequestResults as Option[NonEmptylist[RequestResult]] will solve it.
          val nrs: MarketPlaceAddonsResults = list.toNel.getOrElse(nels(none))
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failNel[MarketPlaceAddonsResults]
      }
    }
  }
}