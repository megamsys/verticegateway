/*
** Copyright [2013-2015] [Megam Systems]
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
import models.base.MarketPlacePlan

/**
 * @author rajthilak
 *
 */

object MarketPlacePlanSerialization extends SerializationBase[MarketPlacePlan] {
  protected val DescriptionKey = "description"
  protected val VersionKey = "version"

  override implicit val writer = new JSONW[MarketPlacePlan] {

    override def write(h: MarketPlacePlan): JValue = {
      JObject(
          JField(DescriptionKey, toJSON(h.description)) ::
          JField(VersionKey, toJSON(h.version)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[MarketPlacePlan] {

    override def read(json: JValue): Result[MarketPlacePlan] = {
      val descriptionField = field[String](DescriptionKey)(json)
      val versionField = field[String](VersionKey)(json)

      (descriptionField |@| versionField) {
        (description: String, version: String) =>
          new MarketPlacePlan(description, version)
      }
    }
  }
}
