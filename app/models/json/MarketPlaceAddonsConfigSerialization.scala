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
import models.{ MarketPlaceAddonsConfig, MarketPlaceAddonsConfigDisaster, MarketPlaceAddonsConfigLoadBalancing, MarketPlaceAddonsConfigAutoScaling, MarketPlaceAddonsConfigMonitoring }

/**
 * @author rajthilak
 *
 */
object MarketPlaceAddonsConfigSerialization extends SerializationBase[MarketPlaceAddonsConfig] {
  protected val DisasterKey = "disaster"
  protected val LoadBalancingKey = "loadbalancing"
  protected val AutoScalingKey = "autoscaling"
  protected val MonitoringKey = "monitoring"

  override implicit val writer = new JSONW[MarketPlaceAddonsConfig] {
    import MarketPlaceAddonsConfigDisasterSerialization.{ writer => MarketPlaceAddonsConfigDisasterWriter }
    import MarketPlaceAddonsConfigLoadBalancingSerialization.{ writer => MarketPlaceAddonsConfigLoadBalancingWriter }
    import MarketPlaceAddonsConfigAutoScalingSerialization.{ writer => MarketPlaceAddonsConfigAutoScalingWriter }
    import MarketPlaceAddonsConfigMonitoringSerialization.{ writer => MarketPlaceAddonsConfigMonitoringWriter }

    override def write(h: MarketPlaceAddonsConfig): JValue = {
      JObject(
        JField(DisasterKey, toJSON(h.disaster)(MarketPlaceAddonsConfigDisasterWriter)) ::
          JField(LoadBalancingKey, toJSON(h.loadbalancing)(MarketPlaceAddonsConfigLoadBalancingWriter)) ::
          JField(AutoScalingKey, toJSON(h.autoscaling)(MarketPlaceAddonsConfigAutoScalingWriter)) ::
          JField(MonitoringKey, toJSON(h.monitoring)(MarketPlaceAddonsConfigMonitoringWriter)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[MarketPlaceAddonsConfig] {
    import MarketPlaceAddonsConfigDisasterSerialization.{ reader => MarketPlaceAddonsConfigDisasterReader }
    import MarketPlaceAddonsConfigLoadBalancingSerialization.{ reader => MarketPlaceAddonsConfigLoadBalancingReader }
    import MarketPlaceAddonsConfigAutoScalingSerialization.{ reader => MarketPlaceAddonsConfigAutoScalingReader }
    import MarketPlaceAddonsConfigMonitoringSerialization.{ reader => MarketPlaceAddonsConfigMonitoringReader }

    override def read(json: JValue): Result[MarketPlaceAddonsConfig] = {
      val disasterField = field[MarketPlaceAddonsConfigDisaster](DisasterKey)(json)(MarketPlaceAddonsConfigDisasterReader)
      val loadBalancingField = field[MarketPlaceAddonsConfigLoadBalancing](LoadBalancingKey)(json)(MarketPlaceAddonsConfigLoadBalancingReader)
      val autoScalingField = field[MarketPlaceAddonsConfigAutoScaling](AutoScalingKey)(json)(MarketPlaceAddonsConfigAutoScalingReader)
      val monitoringField = field[MarketPlaceAddonsConfigMonitoring](MonitoringKey)(json)(MarketPlaceAddonsConfigMonitoringReader)

      (disasterField |@| loadBalancingField |@| autoScalingField |@| monitoringField) {
        (disaster: MarketPlaceAddonsConfigDisaster, loadbalancing: MarketPlaceAddonsConfigLoadBalancing, autoscaling: MarketPlaceAddonsConfigAutoScaling, monitoring: MarketPlaceAddonsConfigMonitoring) =>
          new MarketPlaceAddonsConfig(disaster, loadbalancing, autoscaling, monitoring)
      }
    }
  }
}