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
package controllers.funnel

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

/**
 * @author ram
 *
 */
class FunnelResponseSerialization(charset: Charset = UTF8Charset) extends SerializationBase[FunnelResponse] {
  protected val JSONClazKey = JSON_CLAZ
  protected val CodeKey = "code"
  protected val MessageTypeKey = "msg_type"
  protected val MessageKey = "msg"
  protected val MoreKey = "more"
  protected val LinksKey ="links"  

  override implicit val writer = new JSONW[FunnelResponse] {

    override def write(h: FunnelResponse): JValue = {
      JObject(
        JField(CodeKey, toJSON(h.code)) ::
          JField(MessageTypeKey, toJSON(h.msg_type)) ::
          JField (MessageKey, toJSON(h.msg)) ::
          JField(MoreKey, toJSON(h.more)) ::  JField(JSONClazKey, toJSON(h.json_claz)) :: JField(LinksKey, toJSON(h.links)) ::Nil)
    }
  }

  override implicit val reader = new JSONR[FunnelResponse] {

    override def read(json: JValue): Result[FunnelResponse] = {
      val codeField = field[Int](CodeKey)(json)
      val msgTypeField = field[String](MessageTypeKey)(json)
      val msgField = field[String](MessageKey)(json)
      val moreField = field[String](MoreKey)(json)
      val jsonClazField = field[String](JSONClazKey)(json)

      (codeField |@| msgTypeField |@| msgField |@| moreField |@| jsonClazField) {
        (code: Int, msg: String, more: String, msgType: String,jsonClazField: String) =>
          new FunnelResponse(code, msg, more,msgType,jsonClazField)
      }
    }
  }
}
