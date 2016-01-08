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
import models.json._
import models.analytics.{ WorkbenchesResult, ConnectorsList}
/**
 * @author ranjitha
 *
 */
class WorkbenchesResultSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[WorkbenchesResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ

  protected val IdKey = "id"
  protected val NameKey = "name"
  protected val ConnectorsKey = "connectors"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[WorkbenchesResult] {

    import models.json.analytics.ConnectorsListSerialization.{ writer => ConnectorsListWriter }

    override def write(h: WorkbenchesResult): JValue = {

      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(ConnectorsKey, toJSON(h.connectors)(ConnectorsListWriter)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[WorkbenchesResult] {

import models.json.analytics.ConnectorsListSerialization.{ reader => ConnectorsListReader }

    override def read(json: JValue): Result[WorkbenchesResult] = {

      val idField = field[String](IdKey)(json)
      val nameField = field[String](NameKey)(json)
      val connectorsField = field[ConnectorsList](ConnectorsKey)(json)(ConnectorsListReader)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nameField |@| connectorsField  |@|  createdAtField) {
        (id: String, name: String ,connectors: ConnectorsList , created_at: String) =>
          new WorkbenchesResult(id, name, connectors, created_at)
      }
    }
  }
}
