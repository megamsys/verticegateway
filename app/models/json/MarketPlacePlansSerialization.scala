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
import java.nio.charset.Charset
/**
 * @author rajthilak
 *
 */
object MarketPlacePlansSerialization extends SerializationBase[MarketPlacePlans] {
 
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val ResultsKey = "results"

  implicit override val writer = new JSONW[MarketPlacePlans] {
    override def write(h: MarketPlacePlans): JValue = {
     val nrsList: NonEmptyList[JValue] = h.map {
      //val nrsList: List[String] = h.map {
        //nrOpt: Option[MarketPlacePlan] =>
          //(nrOpt.map { nr: MarketPlacePlan => nr.toJValue }).getOrElse(JNothing)
        nrOpt: MarketPlacePlan =>   nrOpt.toJValue
       // nrOpt: String => nrOpt
      }
     //JObject(JField(ResultsKey, JArray(nrsList.list)) :: Nil)
     JArray(nrsList.list)
     // JObject(JField(JSONClazKey, JString("Megam::MarketPlacePlanCollection")) :: JField(ResultsKey, JArray(nrsList.list)) :: Nil)
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
  implicit override val reader = new JSONR[MarketPlacePlans] {
    override def read(json: JValue): Result[MarketPlacePlans] = {
      play.api.Logger.debug(("%-20s -->[%s]").format("----------------------->", json))
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            MarketPlacePlan.fromJValue(jValue) match {
              //case Success(nr)   => nels(nr)
             // case Failure(fail) => nels(MarketPlacePlan.empty)
              case Success(nr)   => List(nr)
              case Failure(fail) => List[MarketPlacePlan]()
            }
          } map { x: MarketPlacePlan => x }
                  
          //this is screwy. Making the MarketPlacePlans as Option[NonEmptylist[MarketPlacePlan]] will solve it.
         val nrs: MarketPlacePlans = list.toNel.getOrElse(MarketPlacePlans.empty)
          //val nrs: MarketPlacePlans = MarketPlacePlans(list)
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failNel[MarketPlacePlans]
      }
    }
  }
}
