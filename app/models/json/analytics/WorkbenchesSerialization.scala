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
import models.tosca.{   KeyValueList }
import models.analytics.{ Workbenches, ConnectorsList }

/**
 * @author ranjitha
 *
 */
class WorkbenchesSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[Workbenches] {

  protected val NameKey = "name"
  protected val ConnectorsKey = "connectors"


  override implicit val writer = new JSONW[Workbenches] {


    import ConnectorsListSerialization.{ writer => ConnectorsListWriter }


    override def write(h: Workbenches): JValue = {
      JObject(
        JField(NameKey, toJSON(h.name)) ::
          JField(ConnectorsKey, toJSON(h.connectors)(ConnectorsListWriter)) ::
             Nil)
    }
  }

  override implicit val reader = new JSONR[Workbenches] {


    import ConnectorsListSerialization.{ reader => ConnectorsListReader }


    override def read(json: JValue): Result[Workbenches] = {
      val nameField = field[String](NameKey)(json)
      val connectorsField = field[ConnectorsList](ConnectorsKey)(json)(ConnectorsListReader)


      (nameField |@| connectorsField ) {
        (name: String, connectors: ConnectorsList) =>
          new Workbenches(name, connectors)
      }
    }
  }
}
