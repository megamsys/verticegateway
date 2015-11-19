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
import models.analytics.{ Results }
/**
 * @author ranjitha
 *
 */
class SparkjobsResultSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[SparkjobsResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ

  protected val IdKey = "id"
  protected val InputsKey = "inputs"
  protected val SourceKey = "source"
  protected val StatusKey = "status"
  protected val ResultsKey = "results"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[SparkjobsResult] {
    import models.json.tosca.KeyValueListSerialization.{ writer => KeyValueListWriter }
    import models.json.tosca.box.ResultsSerialization.{ writer => ResultsWriter }
    override def write(h: SparkjobsResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(JSONClazKey, toJSON("Megam::Sparkjobs")) ::
          JField(InputsKey, toJSON(h.inputs)(KeyValueListWriter)) ::
          JField(SourceKey, toJSON(h.source)) ::
          JField(StatusKey, toJSON(h.status)) ::
          JField(ResultsKey, toJSON(h.results)(ResultsWriter)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[SparkjobsResult] {

    import models.json.tosca.KeyValueListSerialization.{ reader => KeyValueListReader }
    import models.json.tosca.box.ResultsSerialization.{ reader => ResultsReader }
    override def read(json: JValue): Result[SparkjobsResult] = {

      val idField = field[String](IdKey)(json)
      val inputsField = field[KeyValueList](InputsKey)(json)(KeyValueListReader)
      val sourceField = field[String](SourceKey)(json)
      val statusField = field[String](StatusKey)(json)
      val resultsField = field[Results](ResultsKey)(json)(ResultsReader)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| inputsField |@| sourceField |@| statusField |@| resultsField |@| createdAtField) {
        (id: String, inputs: KeyValueList, source: String, status: String, results: Results, created_at: String) =>
          new SparkjobsResult(id, inputs, source, status, results, created_at)
      }
    }
  }
}
