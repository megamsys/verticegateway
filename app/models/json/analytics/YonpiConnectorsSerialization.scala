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
import models.analytics._
import models.analytics.{YonpiConnector }

/**
 * @author ranjitha
 *
 */

class YonpiConnectorsSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[YonpiConnector] {

  protected val SourceKey = "source"
  protected val CredentialKey = "credential"
  protected val TablesKey = "tables"
  protected val DbnameKey = "dbname"
  protected val EndpointKey = "endpoint"
  protected val PortKey = "port"

  override implicit val writer = new JSONW[YonpiConnector] {
    override def write(h: YonpiConnector): JValue = {
      JObject(
        JField(SourceKey, toJSON(h.source)) ::
        JField(CredentialKey, toJSON(h.credential)) ::
        JField(TablesKey, toJSON(h.tables)) ::
        JField(DbnameKey, toJSON(h.dbname)) ::
        JField(EndpointKey, toJSON(h.endpoint)) ::
        JField(PortKey, toJSON(h.port)) ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[YonpiConnector] {
    override def read(json: JValue): Result[YonpiConnector] = {
      val sourceField = field[String](SourceKey)(json)
      val credentialField = field[String](CredentialKey)(json)
      val tablesField = field[String](TablesKey)(json)
      val dbnameField = field[String](DbnameKey)(json)
      val endpointField = field[String](EndpointKey)(json)
      val portField = field[String](PortKey)(json)

      (sourceField |@| credentialField |@| tablesField |@| dbnameField |@| endpointField |@| portField) {
        (source: String, credential: String, tables: String, dbname: String, endpoint: String, port: String) =>
          new YonpiConnector(source, credential, tables, dbname, endpoint, port)
      }
    }
  }
}
