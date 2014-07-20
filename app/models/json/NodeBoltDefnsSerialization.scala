/* 
** Copyright [2013-2014] [Megam Systems]
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
import models.NodeBoltDefns

/**
 * @author ram
 *
 */
object NodeBoltDefnsSerialization extends SerializationBase[NodeBoltDefns] {
  protected val BoltUserNameKey = "username"
  protected val BoltAPIKey = "apikey"  
  protected val BoltStoreNameKey = "store_name"
  protected val BoltURLKey ="url"
  protected val BoltPrimeKey = "prime"
  protected val BoltTimetoKillKey = "timetokill"
  protected val BoltMeteredKey = "metered"  
  protected val BoltLoggingKey = "logging"
  protected val BoltRuntimeExecKey ="runtime_exec"
  protected val AppEnvShKey ="env_sh"

  override implicit val writer = new JSONW[NodeBoltDefns] {   

    override def write(h: NodeBoltDefns): JValue = {
      JObject(
          JField(BoltUserNameKey, toJSON(h.username)) ::
          JField(BoltAPIKey, toJSON(h.apikey)) ::          
          JField(BoltStoreNameKey, toJSON(h.store_name))    ::
          JField(BoltURLKey, toJSON(h.url)) ::          
          JField(BoltPrimeKey, toJSON(h.prime))    ::
          JField(BoltTimetoKillKey, toJSON(h.timetokill)) ::
          JField(BoltMeteredKey, toJSON(h.metered)) ::          
          JField(BoltLoggingKey, toJSON(h.logging))    ::
          JField(BoltRuntimeExecKey, toJSON(h.runtime_exec))  ::
          JField(AppEnvShKey, toJSON(h.env_sh)) ::Nil)
    }
  }

  override implicit val reader = new JSONR[NodeBoltDefns] {    

    override def read(json: JValue): Result[NodeBoltDefns] = {
      val usernameField = field[String](BoltUserNameKey)(json)
      val apikeyField = field[String](BoltAPIKey)(json)
      val storenameField = field[String](BoltStoreNameKey)(json)
      val urlField = field[String](BoltURLKey)(json)
      val primeField = field[String](BoltPrimeKey)(json)
      val timetokillField = field[String](BoltTimetoKillKey)(json)
      val meterField = field[String](BoltMeteredKey)(json)
      val loggingField = field[String](BoltLoggingKey)(json)
      val runtimeexecField = field[String](BoltRuntimeExecKey)(json)
      val envshField = field[String](AppEnvShKey)(json)
    
      ( usernameField |@| apikeyField |@| storenameField |@| urlField |@| primeField |@| timetokillField |@| meterField |@| loggingField |@| runtimeexecField |@| envshField) {
        (username: String, apikey: String, storename: String, url: String, prime: String, timetokill: String, metered: String, logging: String, runtimeexec: String, env_sh: String) =>
          new NodeBoltDefns(username, apikey, storename, url, prime, timetokill, metered, logging, runtimeexec, env_sh)
      }
    }
  }
}