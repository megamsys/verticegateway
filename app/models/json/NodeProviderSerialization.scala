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
import models.{ NodeProvider }

/**
 * @author ram
 *
 */
object NodeProviderSerialization extends SerializationBase[NodeProvider] {

  protected val ProvKey = "prov"

  override implicit val writer = new JSONW[NodeProvider] {

    override def write(h: NodeProvider): JValue = {
      JObject(
        JField(ProvKey, toJSON(h.prov)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[NodeProvider] {

    override def read(json: JValue): Result[NodeProvider] = {
      val provValSF = field[String](ProvKey)(json)
      provValSF map { provVal => new NodeProvider(provVal) }
    }
  }
}