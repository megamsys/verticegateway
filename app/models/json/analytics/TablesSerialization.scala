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
import models.tosca.{ KeyValueList }
import models.analytics._
import models.analytics.{Tables}

/**
 * @author ranjitha
 *
 */

class TablesSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[Tables] {

  protected val NameKey = "name"
  protected val TableIdKey = "table_id"
  protected val SchemasKey = "schemas"
  protected val LinksKey = "links"

  override implicit val writer = new JSONW[Tables] {

    import models.json.tosca.KeyValueListSerialization.{ writer => KeyValueListWriter }

    override def write(h: Tables): JValue = {
      JObject(
        JField(NameKey, toJSON(h.name)) ::
          JField(TableIdKey, toJSON(h.table_id)) ::
          JField(SchemasKey, toJSON(h.schemas)(KeyValueListWriter)) ::
          JField(LinksKey, toJSON(h.links)(KeyValueListWriter))::
           Nil)
    }
  }

  override implicit val reader = new JSONR[Tables] {

     import models.json.tosca.KeyValueListSerialization.{ reader => KeyValueListReader }

    override def read(json: JValue): Result[Tables] = {
      val NameField = field[String](NameKey)(json)
      val TableIdField = field[String](TableIdKey)(json)
      val SchemasField = field[KeyValueList](SchemasKey)(json)(KeyValueListReader)
      val LinksField = field[KeyValueList](LinksKey)(json)(KeyValueListReader)

      (NameField |@| TableIdField |@| SchemasField |@| LinksField) {
        (name: String, table_id: String, schemas: KeyValueList, links: KeyValueList) =>
          new Tables(name, table_id, schemas, links)
      }
    }
  }
}
