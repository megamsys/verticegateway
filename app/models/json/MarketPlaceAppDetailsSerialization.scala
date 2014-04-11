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
import models.{ MarketPlaceAppDetails }

/**
 * @author rajthilak
 *
 */

object MarketPlaceAppDetailsSerialization extends SerializationBase[MarketPlaceAppDetails] {

  protected val LogoKey = "logo"
  protected val CategoryKey = "category"
  protected val DescriptionKey = "description" 

  override implicit val writer = new JSONW[MarketPlaceAppDetails] {

    override def write(h: MarketPlaceAppDetails): JValue = {
      JObject(
        JField(LogoKey, toJSON(h.logo)) ::
          JField(CategoryKey, toJSON(h.category)) ::
          JField(DescriptionKey, toJSON(h.description)) ::           
           Nil)
    }
  }

  override implicit val reader = new JSONR[MarketPlaceAppDetails] {

    override def read(json: JValue): Result[MarketPlaceAppDetails] = {
      val logoField = field[String](LogoKey)(json)
      val categoryField = field[String](CategoryKey)(json)    
      val descriptionField = field[String](DescriptionKey)(json)     
      
      (logoField |@| categoryField |@| descriptionField ) {
        (logo: String, category: String, description: String) =>
          new MarketPlaceAppDetails(logo, category, description)
      }
    }
  }
}