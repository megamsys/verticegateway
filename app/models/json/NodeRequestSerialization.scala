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
import scalaz.Validation
import scalaz.Validation._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.util.Date
import java.nio.charset.Charset
import controllers.funnel.FunnelErrors._
import controllers.Constants._
import controllers.funnel.SerializationBase
import models.{ NodeResult, NodePredefs, NodeRequest}

/**
 * @author ram
 *
 */
object NodeRequestSerialization extends SerializationBase[NodeRequest] {
  protected val ReqIdKey = "req_id"
  protected val CommandKey = "command"
  

  override implicit val writer = new JSONW[NodeRequest] {

    override def write(h: NodeRequest): JValue = {
      JObject(
        JField(ReqIdKey, toJSON(h.req_id)) ::
          JField(CommandKey, toJSON(h.command))  ::Nil)
    }
  }

  override implicit val reader = new JSONR[NodeRequest] {

    override def read(json: JValue): Result[NodeRequest] = {
      val reqidField = field[String](ReqIdKey)(json)
      val commandField = field[String](CommandKey)(json)
    
      (reqidField |@| commandField) {
        (req_id: String, command: String) =>
          new NodeRequest(req_id, command)
      }
    }
  }
}