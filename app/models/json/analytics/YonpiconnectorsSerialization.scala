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
import models.analytics.{Yonpiconnectors }

/**
 * @author ranjitha
 *
 */

class YonpiconnectorsSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[Yonpiconnectors] {

  protected val SourceKey = "source"
  protected val CredentialsKey = "credentials"
  protected val TablesKey = "tables"
  protected val DbnameKey = "dbname"
  protected val EndpointKey = "endpoint"
  protected val PortKey = "port"

  override implicit val writer = new JSONW[Yonpiconnectors] {
    override def write(h: Yonpiconnectors): JValue = {
      JObject(
        JField(SourceKey, toJSON(h.source)) ::
        JField(CredentialsKey, toJSON(h.credentials)) ::
        JField(TablesKey, toJSON(h.tables)) ::
        JField(DbnameKey, toJSON(h.dbname)) ::
        JField(EndpointKey, toJSON(h.endpoint)) ::
        JField(PortKey, toJSON(h.port)) ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[Yonpiconnectors] {
    override def read(json: JValue): Result[Yonpiconnectors] = {
      val sourceField = field[String](SourceKey)(json)
      val credentialsField = field[String](CredentialsKey)(json)
      val tablesField = field[String](TablesKey)(json)
      val dbnameField = field[String](DbnameKey)(json)
      val endpointField = field[String](EndpointKey)(json)
      val portField = field[String](PortKey)(json)

      (sourceField |@| credentialsField |@| tablesField |@| dbnameField |@| endpointField |@| portField) {
        (source: String, credentials: String, tables: String, dbname: String, endpoint: String, port: String) =>
          new Yonpiconnectors(source, credentials, tables, dbname, endpoint, port)
      }
    }
  }
}
