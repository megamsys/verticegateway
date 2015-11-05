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
package models.json.tosca

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
import models.tosca.{  Metric }

/**
 * @author ranjitha
 *
 */

class MetricSerialization(charset: Charset = UTF8Charset) extends SerializationBase[Metric] {

  protected val MetricTypeKey = "metric_type"
  protected val MetricValueKey = "metric_value"
  protected val MetricUnitsKey = "metric_units"
  protected val MetricNameKey = "metric_name"

  override implicit val writer = new JSONW[Metric] {



    override def write(h: Metric): JValue = {
      JObject(
        JField(MetricTypeKey, toJSON(h.metric_type)) ::
          JField(MetricValueKey, toJSON(h.metric_value)) ::
          JField(MetricUnitsKey, toJSON(h.metric_units)) ::
            JField(MetricNameKey, toJSON(h.metric_name)) ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[Metric] {


    override def read(json: JValue): Result[Metric] = {
      val metrictypeField = field[String](MetricTypeKey)(json)
      val metricvalueField = field[String](MetricValueKey)(json)
      val metricunitsField = field[String](MetricUnitsKey)(json)
  val metricnameField = field[String](MetricNameKey)(json)

      (metrictypeField |@| metricvalueField |@| metricunitsField |@| metricnameField ) {
        (metric_type: String, metric_value: String, metric_units: String, metric_name: String) =>
          new Metric(metric_type, metric_value, metric_units, metric_name)
      }
    }
  }
}
