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
package models.json.billing

import scalaz._
import scalaz.NonEmptyList._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import models.json.billing._
import models.billing._


/**
 * @author ranjitha
 *
 */
object InvoicesResultsSerialization extends io.megam.json.SerializationBase[InvoicesResults] {
  protected val JSONClazKey = models.Constants.JSON_CLAZ
  protected val ResultsKey = "results"

  implicit override val writer = new JSONW[InvoicesResults] {
    override def write(h: InvoicesResults): JValue = {
      val nrsList: NonEmptyList[JValue] = h.map {
        nrOpt: Option[InvoicesResult] =>
          (nrOpt.map { nr: InvoicesResult => nr.toJValue }).getOrElse(JNothing)
      }

      JObject(JField(JSONClazKey, JString("Megam::InvoicesCollection")) :: JField(ResultsKey, JArray(nrsList.list)) :: Nil)
    }
  }


  implicit override val reader = new JSONR[InvoicesResults] {
    override def read(json: JValue): Result[InvoicesResults] = {
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            InvoicesResult.fromJValue(jValue) match {
              case Success(nr)   => List(nr)
              case Failure(fail) => List[InvoicesResult]()
            }
          } map { x: InvoicesResult => x.some }
          //this is screwy. Making the InvoicesResults as Option[NonEmptylist[InvoicesResult]] will solve it.
          val nrs: InvoicesResults = list.toNel.getOrElse(nels(none))
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[InvoicesResults]
      }
    }
  }
}
