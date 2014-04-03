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
import models.{ MarketPlaceAddonsConfigDisaster}
/**
 * @author ram
 *
 */
object MarketPlaceAddonsConfigDisasterSerialization  extends SerializationBase[MarketPlaceAddonsConfigDisaster] {
  protected val LocationsKey = "locations"
  protected val FromHostKey = "fromhost"
  protected val ToHostsKey = "tohosts"  

  override implicit val writer = new JSONW[MarketPlaceAddonsConfigDisaster] {

    override def write(h: MarketPlaceAddonsConfigDisaster): JValue = {
      JObject(
        JField(LocationsKey, toJSON(h.locations)) ::
          JField(FromHostKey, toJSON(h.fromhost)) ::
          JField(ToHostsKey, toJSON(h.tohosts))  ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[MarketPlaceAddonsConfigDisaster] {

    override def read(json: JValue): Result[MarketPlaceAddonsConfigDisaster] = {
      val locationsField = field[String](LocationsKey)(json)
      val fromHostField = field[String](FromHostKey)(json)
      val toHostsField = field[String](ToHostsKey)(json)      

      (locationsField |@| fromHostField |@| toHostsField ) {
        (locations: String, fromhost: String, tohosts: String) =>
          new MarketPlaceAddonsConfigDisaster(locations, fromhost, tohosts)
      }
    }
  }
}