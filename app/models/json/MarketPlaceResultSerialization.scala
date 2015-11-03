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
import models.{ MarketPlaceResult, MarketPlacePlan, MarketPlacePlans }
import models.tosca.{ KeyValueList }

/**
 * @author rajthilak
 *
 */
class MarketPlaceResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[MarketPlaceResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val NameKey = "name"
  protected val CattypeKey = "cattype"
  protected val OrderKey = "order"
  protected val ImageKey = "image"
  protected val UrlKey = "url"
  protected val EnvsKey = "envs"
  protected val PlanKey = "plans"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[MarketPlaceResult] {
    import MarketPlacePlanSerialization.{ writer => MarketPlacePlanWriter }
    import MarketPlacePlansSerialization.{ writer => MarketPlacePlansWriter }
    import models.json.tosca.KeyValueListSerialization.{ writer => KeyValueListWriter }

    override def write(h: MarketPlaceResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(CattypeKey, toJSON(h.cattype)) ::
          JField(OrderKey, toJSON(h.order)) ::
          JField(ImageKey, toJSON(h.image)) ::
          JField(UrlKey, toJSON(h.url)) ::
          JField(EnvsKey, toJSON(h.envs)(KeyValueListWriter)) ::
          JField(JSONClazKey, toJSON("Megam::MarketPlace")) ::
          JField(PlanKey, toJSON(h.plans)(MarketPlacePlansWriter)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[MarketPlaceResult] {
    import MarketPlacePlanSerialization.{ reader => MarketPlacePlanReader }
    import MarketPlacePlansSerialization.{ reader => MarketPlacePlansReader }
      import models.json.tosca.KeyValueListSerialization.{ reader => KeyValueListReader }

    override def read(json: JValue): Result[MarketPlaceResult] = {
      val idField = field[String](IdKey)(json)
      val nameField = field[String](NameKey)(json)
      val cattypeField = field[String](CattypeKey)(json)
      val orderField = field[String](OrderKey)(json)
      val imageField = field[String](ImageKey)(json)
      val urlField = field[String](UrlKey)(json)
      val envsField = field[KeyValueList](EnvsKey)(json)(KeyValueListReader)
      val planField = field[MarketPlacePlans](PlanKey)(json)(MarketPlacePlansReader)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nameField |@| cattypeField |@| orderField |@| imageField |@| urlField |@| envsField |@| planField |@| createdAtField) {
        (id: String, name: String, cattype: String, order: String, image: String, url: String, envs: KeyValueList , plan: MarketPlacePlans, created_at: String) =>
          new MarketPlaceResult(id, name, cattype, order, image, url, envs, plan, created_at)
      }
    }
  }
}
