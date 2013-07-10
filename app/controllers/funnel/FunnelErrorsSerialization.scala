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
class FunnelErrorsSerialization(charset: Charset = UTF8Charset) extends SerializationBase[HttpReturningError] {
  protected val CodeKey = "code"
  protected val ErrorKey = "error"

  override implicit val writer = new JSONW[HttpReturningError] {

    override def write(h: HttpReturningError): JValue = {
      JObject(
        JField(CodeKey, toJSON(h.code)) ::
        JField(ErrorKey, toJSON(h.getMessage)) ::  Nil
      )
    }
  }

  override implicit val reader = new JSONR[HttpReturningError] {

    override def read(json: JValue): Result[HttpReturningError] = {
      val codeField = field[Option[Int]](CodeKey)(json)
      val errorField = field[String](ErrorKey)(json)     

      (codeField |@| errorField) {
        (code: Option[Int], error: String) =>
         /** HttpReturningError(code, errorField) */
          new HttpReturningError(nels(new java.lang.Error("")))
      }
    }
  }
}
