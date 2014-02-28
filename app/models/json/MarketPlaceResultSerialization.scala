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
import models.{ MarketPlaceResult, MarketPlacePlan, MarketPlaceFeatures }

/**
 * @author rajthilak
 *
 */
class MarketPlaceResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[MarketPlaceResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val NameKey = "name"
  protected val LogoKey = "logo"
  protected val CatagoryKey = "catagory"
  protected val PriceTypeKey = "pricetype"
  protected val FeaturesKey = "features" 
  protected val PlanKey = "plan"
  protected val AttachKey = "attach"
  protected val PredefNodeKey = "predefnode"
  protected val ApprovedKey = "approved"    
  protected val CreatedAtKey ="created_at"

  override implicit val writer = new JSONW[MarketPlaceResult] {

    import MarketPlacePlanSerialization.{ writer => MarketPlacePlanWriter }
    import MarketPlaceFeaturesSerialization.{ writer => MarketPlaceFeaturesWriter }

    override def write(h: MarketPlaceResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(LogoKey, toJSON(h.logo)) ::
          JField(JSONClazKey, toJSON("Megam::MarketPlace")) ::
          JField(CatagoryKey, toJSON(h.catagory)) ::
          JField(PriceTypeKey, toJSON(h.pricetype)) ::
          JField(FeaturesKey, toJSON(h.features)(MarketPlaceFeaturesWriter)) ::          
          JField(PlanKey, toJSON(h.plan)(MarketPlacePlanWriter)) ::
          JField(AttachKey, toJSON(h.attach)) ::
          JField(PredefNodeKey, toJSON(h.predefnode)) ::
          JField(ApprovedKey, toJSON(h.approved)) ::
          JField(CreatedAtKey, toJSON(h.created_at))   ::          
           Nil)
    }
  }

  override implicit val reader = new JSONR[MarketPlaceResult] {

    import MarketPlacePlanSerialization.{ reader => MarketPlacePlanReader }
    import MarketPlaceFeaturesSerialization.{ reader => MarketPlaceFeaturesReader }

    override def read(json: JValue): Result[MarketPlaceResult] = {
      val idField = field[String](IdKey)(json)
      val nameField = field[String](NameKey)(json)
      val logoField = field[String](LogoKey)(json)
      val catagoryField = field[String](CatagoryKey)(json)
      val pricetypeField = field[String](PriceTypeKey)(json)
      val featuresField = field[MarketPlaceFeatures](FeaturesKey)(json)(MarketPlaceFeaturesReader)      
      val planField = field[MarketPlacePlan](PlanKey)(json)(MarketPlacePlanReader)
      val attachField = field[String](AttachKey)(json)
      val predefNodeField = field[String](PredefNodeKey)(json)
      val approvedField = field[String](ApprovedKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nameField |@| logoField |@| catagoryField |@| pricetypeField |@| featuresField |@| planField |@| attachField |@| predefNodeField |@| approvedField |@| createdAtField) {
        (id: String, name: String, logo: String, catagory: String, pricetype: String, features: MarketPlaceFeatures, plan: MarketPlacePlan, attach: String, predefnode: String, approved: String, created_at: String) =>
          new MarketPlaceResult(id, name, logo, catagory, pricetype, features, plan, attach, predefnode, approved, created_at)
      }
    }
  }
}