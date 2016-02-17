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
 * @author ram
 *
 */
object SshKeysResultsSerialization extends io.megam.json.SerializationBase[SshKeysResults] {
  protected val JSONClazKey = models.Constants.JSON_CLAZ
  protected val ResultsKey = "results"

  implicit override val writer = new JSONW[SshKeysResults] {
    override def write(h: SshKeysResults): JValue = {
      val nrsList: NonEmptyList[JValue] = h.map {
        nrOpt: Option[SshKeysResult] =>
          (nrOpt.map { nr: SshKeysResult => nr.toJValue }).getOrElse(JNothing)
      }

      JObject(JField(JSONClazKey, JString("Megam::SshKeysCollection")) :: JField(ResultsKey, JArray(nrsList.list)) :: Nil)
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
  implicit override val reader = new JSONR[SshKeysResults] {
    override def read(json: JValue): Result[SshKeysResults] = {
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            SshKeysResult.fromJValue(jValue) match {
              case Success(nr)   => List(nr)
              case Failure(fail) => List[SshKeysResult]()
            }
          } map { x: SshKeysResult => x.some }
          //this is screwy. Making the PredefCloudResults as Option[NonEmptylist[NodeResult]] will solve it.
          val nrs: SshKeysResults = list.toNel.getOrElse(nels(none))
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[SshKeysResults]
      }
    }
  }
}
