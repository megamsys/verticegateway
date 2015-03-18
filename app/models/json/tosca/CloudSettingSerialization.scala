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
package models.json.tosca

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
import controllers.funnel.SerializationBase
import models.tosca.{ CloudSetting, CloudSettingsList, CSWiresList }

/**
 * @author rajthilak
 *
 */
class CloudSettingSerialization(charset: Charset = UTF8Charset) extends SerializationBase[CloudSetting] {

  protected val IdKey = "id"
  protected val CsTypeKey = "cstype"
  protected val CloudSettingsKey = "cloudsettings"
  protected val XKey = "x"
  protected val YKey = "y"
  protected val ZKey = "z"
  protected val WiresKey = "wires"
    
  override implicit val writer = new JSONW[CloudSetting] {
    
 import CSWiresListSerialization.{ writer => CSWiresListWriter }
 
    override def write(h: CloudSetting): JValue = {
      JObject(
          JField(IdKey, toJSON(h.id)) ::
          JField(CsTypeKey, toJSON(h.cstype)) ::
          JField(CloudSettingsKey, toJSON(h.cloudsettings)) ::
          JField(XKey, toJSON(h.x)) ::
          JField(YKey, toJSON(h.y)) ::
          JField(ZKey, toJSON(h.z)) ::
          JField(WiresKey, toJSON(h.wires)(CSWiresListWriter)) ::
           Nil)
    }
  }

  override implicit val reader = new JSONR[CloudSetting] {
    
     import CSWiresListSerialization.{ reader => CSWiresListReader }

    override def read(json: JValue): Result[CloudSetting] = {
      val idField = field[String](IdKey)(json)
      val csTypeField = field[String](CsTypeKey)(json)
      val cloudsettingField = field[String](CloudSettingsKey)(json)
      val xField = field[String](XKey)(json)  
      val yField = field[String](YKey)(json)
      val zField = field[String](ZKey)(json)
      val wiresField = field[CSWiresList](WiresKey)(json)(CSWiresListReader)
      

      (idField |@| csTypeField |@| cloudsettingField |@| xField |@| yField |@| zField |@| wiresField) {
          (id: String, cstype: String, cloudsettings: String, x: String, y: String, z: String, wires: CSWiresList) =>
          new CloudSetting(id, cstype, cloudsettings, x, y, z, wires)
      }
    }
  }
}