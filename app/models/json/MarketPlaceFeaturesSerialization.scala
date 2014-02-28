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
import models.{ MarketPlaceFeatures }

/**
 * @author rajthilak
 *
 */

object MarketPlaceFeaturesSerialization extends SerializationBase[MarketPlaceFeatures] {

  protected val Feature1Key = "feature1"
  protected val Feature2Key = "feature2"
  protected val Feature3Key = "feature3"
  protected val Feature4Key = "feature4"

  override implicit val writer = new JSONW[MarketPlaceFeatures] {

    override def write(h: MarketPlaceFeatures): JValue = {
      JObject(
        JField(Feature1Key, toJSON(h.feature1)) ::
          JField(Feature2Key, toJSON(h.feature2)) ::
          JField(Feature3Key, toJSON(h.feature3)) :: 
          JField(Feature4Key, toJSON(h.feature4)) :: 
           Nil)
    }
  }

  override implicit val reader = new JSONR[MarketPlaceFeatures] {

    override def read(json: JValue): Result[MarketPlaceFeatures] = {
      val feature1Field = field[String](Feature1Key)(json)
      val feature2Field = field[String](Feature2Key)(json)    
      val feature3Field = field[String](Feature3Key)(json)
      val feature4Field = field[String](Feature4Key)(json)
      
      (feature1Field |@| feature2Field |@| feature3Field |@| feature4Field) {
        (feature1: String, feature2: String, feature3: String, feature4: String) =>
          new MarketPlaceFeatures(feature1, feature2, feature3, feature4)
      }
    }
  }
}