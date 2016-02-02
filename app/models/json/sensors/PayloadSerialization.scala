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
package models.json.sensors

import scalaz._
import scalaz.NonEmptyList._
import scalaz.Validation
import scalaz.Validation._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.util.Date
import java.nio.charset.Charset
import io.megam.auth.funnel.FunnelErrors._
import controllers.Constants._
import models.tosca.{ Payload, MetricList }

/**
 * @author ranjitha
 *
 */

object PayloadSerialization extends io.megam.json.SerializationBase[Payload] {

  protected val AccountsIdKey = "accounts_id"
  protected val AssembliesIdKey = "assemblies_id"
  protected val AssemblyIdKey = "assembly_id"
  protected val ComponentIdKey = "component_id"
  protected val StateKey = "state"
  protected val SourceKey = "source"
  protected val NodeKey = "node"
  protected val MessageKey = "message"
  protected val AuditPeriodBeginingKey = "audit_period_begining"
    protected val AuditPeriodEndingKey = "audit_period_ending"
    protected val MetricsKey = "metrics"

  override implicit val writer = new JSONW[Payload] {

    import models.json.sensors.MetricListSerialization.{ writer => MetricListWriter }

    override def write(h: Payload): JValue = {
      JObject(
        JField(AccountsIdKey, toJSON(h.accounts_id)) ::
          JField(AssembliesIdKey, toJSON(h.assemblies_id)) ::
          JField(AssemblyIdKey, toJSON(h.assembly_id)) ::
          JField(ComponentIdKey, toJSON(h.component_id)) ::
            JField(StateKey, toJSON(h.state)) ::
              JField(SourceKey, toJSON(h.source)) ::
                JField(NodeKey, toJSON(h.node)) ::
                  JField(MessageKey, toJSON(h.message)) ::
                  JField(AuditPeriodBeginingKey, toJSON(h.audit_period_begining)) ::
                    JField(AuditPeriodEndingKey, toJSON(h.audit_period_ending)) ::
          JField(MetricsKey, toJSON(h.metrics)(MetricListWriter)) ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[Payload] {

    import models.json.sensors.MetricListSerialization.{ reader => MetricListReader }

    override def read(json: JValue): Result[Payload] = {
      val accountsIdField = field[String](AccountsIdKey)(json)
      val assembliesIdField = field[String](AssembliesIdKey)(json)
        val assemblyIdField = field[String](AssemblyIdKey)(json)
          val componentIdField = field[String](ComponentIdKey)(json)
            val stateField = field[String](StateKey)(json)
              val sourceField = field[String](SourceKey)(json)
                val nodeField = field[String](NodeKey)(json)
                  val messageField = field[String](MessageKey)(json)
                  val auditPeriodBeginingField = field[String](AuditPeriodBeginingKey)(json)
                    val auditPeriodEndingField = field[String](AuditPeriodEndingKey)(json)
                 val metricsField = field[MetricList](MetricsKey)(json)(MetricListReader)

      (accountsIdField |@| assembliesIdField |@| assemblyIdField |@| componentIdField |@| stateField |@| sourceField |@| nodeField |@| messageField |@| auditPeriodBeginingField |@| auditPeriodEndingField |@| metricsField) {
        (accounts_id: String, assemblies_id: String, assembly_id: String, component_id: String, state: String, source: String, node: String, message: String, audit_period_begining: String, audit_period_ending: String, metrics: MetricList) =>
          new Payload(accounts_id, assemblies_id, assembly_id, component_id, state, source, node, message, audit_period_begining, audit_period_ending, metrics)
      }
    }
  }
}
