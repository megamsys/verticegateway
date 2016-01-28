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
import models.json.tosca._
import models.json.analytics._
import models.analytics.{ Yonpiinput, YonpiConnectorsList }

/**
 * @author ranjitha
 *
 */
class YonpiinputSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[Yonpiinput] {

  protected val QueryKey = "query"
  protected val ConnectorsKey = "connectors"


  override implicit val writer = new JSONW[Yonpiinput] {


    import YonpiConnectorsListSerialization.{ writer => YonpiConnectorsListWriter }


    override def write(h: Yonpiinput): JValue = {
      JObject(
        JField(QueryKey, toJSON(h.query)) ::
          JField(ConnectorsKey, toJSON(h.connectors)(YonpiConnectorsListWriter)) ::
             Nil)
    }
  }

  override implicit val reader = new JSONR[Yonpiinput] {


    import YonpiConnectorsListSerialization.{ reader => YonpiConnectorsListReader }


    override def read(json: JValue): Result[Yonpiinput] = {
      val queryField = field[String](QueryKey)(json)
      val connectorsField = field[YonpiConnectorsList](ConnectorsKey)(json)(YonpiConnectorsListReader)


      (queryField |@| connectorsField ) {
        (query: String, connectors: YonpiConnectorsList) =>
          new Yonpiinput(query, connectors)
      }
    }
  }
}
