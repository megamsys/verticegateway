/* 
** Copyright [2012-2013] [Megam Systems]
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
package models.json

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
import org.megam.common.enumeration._
import models.NodeAppDefns

/**
 * @author ram
 *
 */
object NodeAppDefnsSerialization extends SerializationBase[NodeAppDefns] {
  protected val AppTimetoKillKey = "timetokill"
  protected val AppMeteredKey = "metered"  
  protected val AppLoggingKey = "logging"
  protected val AppRuntimeExecKey ="runtime_exec"
  

  override implicit val writer = new JSONW[NodeAppDefns] {   

    override def write(h: NodeAppDefns): JValue = {
      JObject(
        JField(AppTimetoKillKey, toJSON(h.timetokill)) ::
          JField(AppMeteredKey, toJSON(h.metered)) ::          
          JField(AppLoggingKey, toJSON(h.logging))    ::
          JField(AppRuntimeExecKey, toJSON(h.runtime_exec))  ::Nil)
    }
  }

  override implicit val reader = new JSONR[NodeAppDefns] {    

    override def read(json: JValue): Result[NodeAppDefns] = {
      val timetokillField = field[String](AppTimetoKillKey)(json)
      val meterField = field[String](AppMeteredKey)(json)
      val loggingField = field[String](AppLoggingKey)(json)
      val runtimeexecField = field[String](AppRuntimeExecKey)(json)
    
      ( timetokillField |@| meterField |@| loggingField |@| runtimeexecField ) {
        (timetokill: String, metered: String, logging: String, runtimeexec: String) =>
          new NodeAppDefns(timetokill, metered, logging, runtimeexec)
      }
    }
  }
}