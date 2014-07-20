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
import java.nio.charset.Charset
import controllers.Constants._

/**
 * @author ram
 *
 */
class CloudInstructionGroupSerialization(charset: Charset = UTF8Charset) extends SerializationBase[CloudInstructionGroup] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val ResultsKey = "results"
  protected val GroupKey = "group"

  import models.json.CloudInstructionsSerialization

  implicit override val writer = new JSONW[CloudInstructionGroup] {
    override def write(h: CloudInstructionGroup): JValue = {
      val cis = new CloudInstructionsSerialization()
      val cisOpt: Option[List[(String, List[JValue])]] = (h.mapValues {
        cr: CloudInstructions => List(toJSON(cr)(cis.writer))
      }).toList.some

      JArray((for {
        cf <- cisOpt
      } yield {
        cf.map {
          r =>
            JObject(
              JField(JSONClazKey, JString("Megam::CloudInstructionGroup")) ::
                JField(GroupKey, JString(r._1)) :: JField(ResultsKey, JArray(r._2)) :: Nil)
        }
      }).getOrElse(List(JNothing)))
    }
  }

  // The reader isn't tested.
  implicit override val reader = new JSONR[CloudInstructionGroup] {
    override def read(json: JValue): Result[CloudInstructionGroup] = {
      json match {
        case JArray(jObjectList) => {

          val list: List[(String, CloudInstructions)] = jObjectList.flatMap { jValue: JValue =>
            CloudInstructions.fromJValue(jValue) match {
              case Success(nr)   => List(((jValue.asInstanceOf[JField]).name, nr))
              case Failure(fail) => List(((jValue.asInstanceOf[JField]).name, CloudInstructions.empty))
            }
          }
          val nrs: CloudInstructionGroup = list.toMap
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failNel[CloudInstructionGroup]
      }
    }
  }
}