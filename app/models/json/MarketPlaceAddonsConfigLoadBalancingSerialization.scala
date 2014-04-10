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
import models.{ MarketPlaceAddonsConfigLoadBalancing }
/**
 * @author rajthilak
 *
 */
object MarketPlaceAddonsConfigLoadBalancingSerialization  extends SerializationBase[MarketPlaceAddonsConfigLoadBalancing] {
  protected val HaproxyHostKey = "haproxyhost"
  protected val LoadBalanceHostKey = "loadbalancehost" 
  protected val Recipe  = "recipe" 

  override implicit val writer = new JSONW[MarketPlaceAddonsConfigLoadBalancing] {

    override def write(h: MarketPlaceAddonsConfigLoadBalancing): JValue = {
      JObject(
        JField(HaproxyHostKey, toJSON(h.haproxyhost)) ::
          JField(LoadBalanceHostKey, toJSON(h.loadbalancehost)) :: 
          JField(Recipe, toJSON(h.recipe)) ::        
           Nil)
    }
  }

  override implicit val reader = new JSONR[MarketPlaceAddonsConfigLoadBalancing] {

    override def read(json: JValue): Result[MarketPlaceAddonsConfigLoadBalancing] = {
      val haproxyhostField = field[String](HaproxyHostKey)(json)
      val loadBalanceHostField = field[String](LoadBalanceHostKey)(json) 
      val recipeField = field[String](Recipe)(json)     

      (haproxyhostField |@| loadBalanceHostField |@| recipeField) {
        (haproxyhost: String, loadbalancehost: String, recipe: String) =>
          new MarketPlaceAddonsConfigLoadBalancing(haproxyhost, loadbalancehost, recipe)
      }
    }
  }
}