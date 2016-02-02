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
import scala.collection.mutable.ListBuffer
import java.nio.charset.Charset
import models.team._

/**
 * @author morpheyesh
 *
 */
object RelatedOrgsListSerialization extends io.megam.json.SerializationBase[RelatedOrgsList] {
  implicit val formats = DefaultFormats

  implicit override val writer = new JSONW[RelatedOrgsList] {
    override def write(h: RelatedOrgsList): JValue = {
      val nrsList: Option[List[JValue]] = h.map {
        nrOpt: String => toJSON(nrOpt)
      }.some

      JArray(nrsList.getOrElse(List.empty[JValue]))
    }
  }


   implicit override val reader = new JSONR[RelatedOrgsList] {
    override def read(json: JValue): Result[RelatedOrgsList] = {
      var list = new ListBuffer[String]()
      json match {
        case JArray(jObjectList) => {
         jObjectList.foreach { jValue: JValue =>
            list += jValue.extract[String]
          }.some

          val nrs: RelatedOrgsList = list.toList
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[RelatedOrgsList]
      }
    }
  }
}
