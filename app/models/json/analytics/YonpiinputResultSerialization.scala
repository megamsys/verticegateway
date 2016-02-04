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
import models.json._
import models.analytics.{ YonpiinputResult, YonpiConnectorsList}
/**
 * @author ranjitha
 *
 */
class YonpiinputResultSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[YonpiinputResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ

  protected val IdKey = "id"
  protected val QueryKey = "query"
  protected val ConnectorsKey = "connectors"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[YonpiinputResult] {

    import models.json.analytics.YonpiConnectorsListSerialization.{ writer => YonpiConnectorsListWriter }

    override def write(h: YonpiinputResult): JValue = {

      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(QueryKey, toJSON(h.query)) ::
          JField(ConnectorsKey, toJSON(h.connectors)(YonpiConnectorsListWriter)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) ::
          Nil)
    }
  }

  override implicit val reader = new JSONR[YonpiinputResult] {

import models.json.analytics.YonpiConnectorsListSerialization.{ reader => YonpiConnectorsListReader }

    override def read(json: JValue): Result[YonpiinputResult] = {

      val idField = field[String](IdKey)(json)
      val queryField = field[String](QueryKey)(json)
      val connectorsField = field[YonpiConnectorsList](ConnectorsKey)(json)(YonpiConnectorsListReader)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| queryField |@| connectorsField  |@|  createdAtField) {
        (id: String, query: String ,connectors: YonpiConnectorsList , created_at: String) =>
          new YonpiinputResult(id, query, connectors, created_at)
      }
    }
  }
}
