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
import models.tosca.{   KeyValueList }
import models.analytics._
import models.analytics.{ Connectors }

/**
 * @author ranjitha
 *
 */

class ConnectorsSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[Connectors] {

  protected val SourceKey = "source"
  protected val EndpointKey = "endpoint"
  protected val PortKey    = "port"
  protected val DbnameKey  = "dbname"
  protected val InputsKey = "inputs"
  protected val TablesKey = "tables"

  override implicit val writer = new JSONW[Connectors] {

    import models.json.tosca.KeyValueListSerialization.{ writer => KeyValueListWriter }
      import TablesListSerialization.{ writer => TablesListWriter }


    override def write(h: Connectors): JValue = {
      JObject(
        JField(SourceKey, toJSON(h.source)) ::
          JField(EndpointKey, toJSON(h.endpoint)) ::
          JField(PortKey, toJSON(h.port)) ::
          JField(DbnameKey, toJSON(h.dbname)) ::
          JField(InputsKey, toJSON(h.inputs)(KeyValueListWriter)) ::
          JField(TablesKey, toJSON(h.tables)(TablesListWriter))::
           Nil)
    }
  }

  override implicit val reader = new JSONR[Connectors] {

     import models.json.tosca.KeyValueListSerialization.{ reader => KeyValueListReader }
     import TablesListSerialization.{ reader => TablesListReader}

    override def read(json: JValue): Result[Connectors] = {
      val sourceField = field[String](SourceKey)(json)
      val endpointField = field[String](EndpointKey)(json)
      val portField = field[String](PortKey)(json)
      val dbnameField = field[String](DbnameKey)(json)
      val inputsField = field[KeyValueList](InputsKey)(json)(KeyValueListReader)
      val tablesField = field[TablesList](TablesKey)(json)(TablesListReader)

      (sourceField |@| endpointField |@| portField |@| dbnameField |@| inputsField |@| tablesField) {
        (source: String, endpoint: String, port: String, dbname: String, inputs: KeyValueList, tables: TablesList) =>
          new Connectors(source, endpoint, port, dbname, inputs, tables)
      }
    }
  }
}
