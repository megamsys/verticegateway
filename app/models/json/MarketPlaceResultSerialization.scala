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
import controllers.funnel.SerializationBase
import models.{ MarketPlaceResult, MarketPlacePlan, MarketPlaceCatalog, MarketPlacePlans }

/**
 * @author rajthilak
 *
 */
class MarketPlaceResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[MarketPlaceResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val NameKey = "name"
  protected val CatalogKey = "catalog"
  protected val PlanKey = "plans"
  protected val CattypeKey = "cattype"
  protected val PredefKey = "predef"
  protected val StatusKey = "status"
  protected val CreatedAtKey ="created_at"

  override implicit val writer = new JSONW[MarketPlaceResult] {
    import MarketPlacePlanSerialization.{ writer => MarketPlacePlanWriter }
    import MarketPlacePlansSerialization.{ writer => MarketPlacePlansWriter }
    import MarketPlaceCatalogSerialization.{ writer => MarketPlaceCatalogWriter }

    override def write(h: MarketPlaceResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(CatalogKey, toJSON(h.catalog)(MarketPlaceCatalogWriter)) ::
          JField(JSONClazKey, toJSON("Megam::MarketPlace")) ::
          JField(PlanKey, toJSON(h.plans)(MarketPlacePlansWriter)) ::
          JField(CattypeKey, toJSON(h.cattype)) ::
          JField(PredefKey, toJSON(h.predef)) ::
          JField(StatusKey, toJSON(h.status)) ::
          JField(CreatedAtKey, toJSON(h.created_at))   ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[MarketPlaceResult] {
     import MarketPlacePlanSerialization.{ reader => MarketPlacePlanReader }
    import MarketPlacePlansSerialization.{ reader => MarketPlacePlansReader }
    import MarketPlaceCatalogSerialization.{ reader => MarketPlaceCatalogReader }

    override def read(json: JValue): Result[MarketPlaceResult] = {
      val idField = field[String](IdKey)(json)
      val nameField = field[String](NameKey)(json)
      val catalogField = field[MarketPlaceCatalog](CatalogKey)(json)(MarketPlaceCatalogReader)
      val planField = field[MarketPlacePlans](PlanKey)(json)(MarketPlacePlansReader)
      val cattypeField = field[String](CattypeKey)(json)
      val predefField = field[String](PredefKey)(json)
      val statusField = field[String](StatusKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nameField |@| catalogField |@| planField |@| cattypeField |@| predefField |@| statusField |@| createdAtField) {
        (id: String, name: String, catalog: MarketPlaceCatalog, plan: MarketPlacePlans, cattype: String, predef: String, status: String, created_at: String) =>
          new MarketPlaceResult(id, name, catalog, plan, cattype, predef, status, created_at)
      }
    }
  }
}
