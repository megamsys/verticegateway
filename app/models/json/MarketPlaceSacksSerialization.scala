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

package models.json

import scalaz._
import scalaz.NonEmptyList._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import models.base._

/**
 * @author morpheyesh
 *
 */

object MarketPlaceSacksSerialization extends io.megam.json.SerializationBase[MarketPlaceSacks] {
  protected val ResultsKey = "results"
  protected val JSONClazKey = models.Constants.JSON_CLAZ

  implicit override val writer = new JSONW[MarketPlaceSacks] {
    override def write(h: MarketPlaceSacks): JValue = {
      val nrsList: NonEmptyList[JValue] = h.map {
        nrOpt: Option[MarketPlaceSack] =>
          (nrOpt.map { nr: MarketPlaceSack => nr.toJValue }).getOrElse(JNothing)
      }

      JObject(JField(JSONClazKey, JString("Megam::MarketPlaceCollection")) :: JField(ResultsKey, JArray(nrsList.list)) :: Nil)
    }
  }

  implicit override val reader = new JSONR[MarketPlaceSacks] {
    override def read(json: JValue): Result[MarketPlaceSacks] = {
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            MarketPlaceSack.fromJValue(jValue) match {
              case Success(nr) => List(nr)
              case Failure(fail) => List[MarketPlaceSack]()
            }
          } map { x: MarketPlaceSack => x.some }
          //this is screwy. Making the MarketPlaceResults as Option[NonEmptylist[MarketPlaceResult]] will solve it.
          val nrs: MarketPlaceSacks = list.toNel.getOrElse(nels(none))
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[MarketPlaceSacks]
      }
    }
  }

}
