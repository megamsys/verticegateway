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
import models.tosca.{ EventsResult }

/**
 * @author morpheyesh
 *
 */
class EventsResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[EventsResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  
      protected val IdKey = "id"
      protected val AssemblyIdKey = "a_id"
      protected val AssemblyNameKey = "a_name"
      protected val CommandKey = "command"
      protected val LaunchTypeKey = "launch_type"
      protected val CreatedAtKey ="created_at"
 

  override implicit val writer = new JSONW[EventsResult] {
   
    override def write(h: EventsResult): JValue = {
      JObject(
           JField(IdKey, toJSON(h.id)) ::
           JField(AssemblyIdKey, toJSON(h.a_id)) ::
           JField(JSONClazKey, toJSON("Megam::Event")) ::
           JField(AssemblyNameKey, toJSON(h.a_name)) ::
           JField(CommandKey, toJSON(h.command)) ::
           JField(LaunchTypeKey, toJSON(h.launch_type)) ::
           JField(CreatedAtKey, toJSON(h.created_at))   ::          
          Nil)
    }
  }

  override implicit val reader = new JSONR[EventsResult] {
   
    
    

    override def read(json: JValue): Result[EventsResult] = {
      
       val idField = field[String](IdKey)(json)
       val assemblyIdField = field[String](AssemblyIdKey)(json)
      val assemblyNameField = field[String](AssemblyNameKey)(json)
      val commandField = field[String](CommandKey)(json)
      val launchTypeField = field[String](LaunchTypeKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)

      

      (idField |@|assemblyIdField |@|assemblyNameField |@|commandField |@| launchTypeField |@| createdAtField) {
        (id: String, a_id: String, a_name: String, command: String, launch_type: String, created_at: String) =>
          new EventsResult(id, a_id, a_name, command, launch_type, created_at)
      }
    }
  }
}