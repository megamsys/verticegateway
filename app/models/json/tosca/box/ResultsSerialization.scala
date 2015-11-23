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
package models.json.tosca.box

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
import models.analytics.{ Results }

/**
 * @author ranjitha
 *
 */

object ResultsSerialization extends models.json.SerializationBase[Results] {

  protected val JobIdKey = "job_id"
  protected val ContextKey = "context"

  override implicit val writer = new JSONW[Results] {

    override def write(h: Results): JValue = {
      JObject(
        JField(JobIdKey, toJSON(h.job_id)) ::
          JField(ContextKey, toJSON(h.context)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[Results] {

    override def read(json: JValue): Result[Results] = {
      val jobidField = field[String](JobIdKey)(json)
      val contextField = field[String](ContextKey)(json)

      (jobidField |@| contextField) {
        (jobid: String, context: String) =>
          new Results(jobid, context)
      }
    }
  }
}
