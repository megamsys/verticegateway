/*
** Copyright [2013-2016] [Megam Systems]
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
import java.nio.charset.Charset
import io.megam.auth.funnel.FunnelErrors._
import models.Constants._
import models.base.{ MarketPlaceSack }
import models.tosca.{ KeyValueList }
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.util.Date

class MarketPlaceSackSerialization(charset: Charset = UTF8Charset) extends io.megam.json.SerializationBase[MarketPlaceSack] {
  protected val SettingsNameKey = "settings_name"
  protected val CattypeKey = "cattype"
  protected val FlavorKey = "flavor"
  protected val ImageKey = "image"
  protected val UrlKey = "url"
  protected val EnvsKey = "envs"
  protected val PlansKey = "plans"

  override implicit val writer = new JSONW[MarketPlaceSack] {
    import models.json.tosca.KeyValueListSerialization.{ writer => KeyValueListWriter }

    override def write(h: MarketPlaceSack): JValue = {
      JObject(
        JField(SettingsNameKey, toJSON(h.settings_name)) ::
          JField(CattypeKey, toJSON(h.cattype)) ::
          JField(FlavorKey, toJSON(h.flavor)) ::
          JField(ImageKey, toJSON(h.image)) ::
          JField(UrlKey, toJSON(h.url)) ::
          JField(EnvsKey, toJSON(h.envs)) ::
          JField(PlansKey, toJSON(h.plans)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[MarketPlaceSack] {
  //  import MarketPlacePlanSerialization.{ reader => MarketPlacePlanReader }
  //  import MarketPlacePlansSerialization.{ reader => MarketPlacePlansReader }
      import models.json.tosca.KeyValueListSerialization.{ reader => KeyValueListReader }

    override def read(json: JValue): Result[MarketPlaceSack] = {
      val settings_nameField = field[String](SettingsNameKey)(json)
      val cattypeField = field[String](CattypeKey)(json)
      val flavorField = field[String](FlavorKey)(json)
      val imageField = field[String](ImageKey)(json)
      val urlField = field[String](UrlKey)(json)
      val envsField = field[Map[String, String]](EnvsKey)(json)
      val plansField = field[Map[String, String]](PlansKey)(json)//(MarketPlacePlansReader)

      ( settings_nameField |@| cattypeField |@| flavorField |@| imageField |@| urlField |@| envsField |@| plansField) {
        (settings_name: String, cattype: String, flavor: String, image: String, url: String, envs: Map[String, String] , plans: Map[String, String]) =>
          new MarketPlaceSack(settings_name, cattype, flavor, image, url, envs, plans )
      }
    }
  }

}
