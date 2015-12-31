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
package models.json.analytics

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
import models.tosca._
import models.analytics._
import models.tosca.{ KeyValueList }
/**
 * @author ranjitha
 *
 */
class SparkjobsResultSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[SparkjobsResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ

  protected val IdKey = "id"
  protected val CodeKey = "code"
  protected val StatusKey = "status"
  protected val JobIdKey = "job_id"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[SparkjobsResult] {
    override def write(h: SparkjobsResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(CodeKey, toJSON(h.code)) ::
          JField(StatusKey, toJSON(h.status)) ::
          JField(JobIdKey, toJSON(h.job_id)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[SparkjobsResult] {

    override def read(json: JValue): Result[SparkjobsResult] = {

      val idField = field[String](IdKey)(json)
      val codeField = field[Int](CodeKey)(json)
      val statusField = field[String](StatusKey)(json)
      val jobIdField = field[String](JobIdKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| codeField |@| statusField |@| jobIdField |@|  createdAtField) {
        (id: String, code: Int, status: String ,job_id: String, created_at: String) =>
          new SparkjobsResult(id, code, status, job_id, created_at)
      }
    }
  }
}
