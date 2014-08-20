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
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.util.Date
import java.nio.charset.Charset
import controllers.funnel.FunnelErrors._
import controllers.Constants._
import controllers.funnel.SerializationBase
import org.megam.common.enumeration._
import models.NodeStatusType

/**
 * @author ram
 *
 */
object NodeStatusTypeSerialization extends SerializationBase[NodeStatusType] {
  implicit override val reader = new JSONR[NodeStatusType] {
    override def read(jValue: JValue): ValidationNel[Error, NodeStatusType] = jValue match {
      case JString(s) => s.readEnum[NodeStatusType].map(_.successNel[Error]) | {
        play.api.Logger.debug(("%-20s -->[%s]").format("status reader", jValue))
        UncategorizedError("request type", "unknown request type %s".format(s), List()).failureNel[NodeStatusType]
      }
      case _ => NoSuchFieldError("request type", jValue).failureNel[NodeStatusType]
    }
  }

  implicit override val writer = new JSONW[NodeStatusType] {
    override def write(t: NodeStatusType): JValue = JString(t.stringVal)
  }
}