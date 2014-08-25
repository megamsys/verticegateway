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
package controllers.funnel

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
//import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import org.megam.common.jsonscalaz._
import play.api.http.Status._

/**
 * @author ram
 *
 */
case class FunnelResponse(code: Int, msg: String, more: String, json_claz: String,
    msg_type: String = "error", links: String = tailMsg) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import controllers.funnel.FunnelResponseSerialization
    val funser = new FunnelResponseSerialization()
    toJSON(this)(funser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }

}

object FunnelResponse {

  def apply(code: Int, message: String, json_claz: String): FunnelResponse = new FunnelResponse(code, message, new String(), json_claz, "info")
  
  def apply(message: String, json_claz: String): FunnelResponse = FunnelResponse(OK, message, json_claz)

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[FunnelResponse] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import controllers.funnel.FunnelResponseSerialization
    val funser = new FunnelResponseSerialization()
    fromJSON(jValue)(funser.reader)
  }

  def fromJson(json: String): Result[FunnelResponse] = (Validation.fromTryCatch[net.liftweb.json.JValue] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

   
}