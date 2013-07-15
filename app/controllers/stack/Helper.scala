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
package controllers.stack

import scalaz._
import Scalaz._
import play.api._
import play.api.mvc._
import models._
import scalaz.Validation._
import play.api.mvc.Result
import org.megam.common.amqp._
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import org.megam.common.uid._
/**
 * @author rajthilak
 *
 */
trait Helper {

  private val SFHOST = MConfig.snowflakeHost
  private val SFPORT: Int = MConfig.snowflakePort
  val HMAC_HEADER = "X-Megam-HMAC"

  def getAccountID(sentHmacHeader: Option[String]): String = {
    ""  }

  def getUID(agent: String): String = {
    val uid = UID(SFHOST, SFPORT, agent)
    val id = uid.get
    val res: UniqueID = id match {
      case Success(uid) => {
        uid
      }
      case Failure(error) => {
        None
      }
    }
    (res.get._1 + res.get._2)
  }

  def getPredefJSON(id: String, name: String, provider: String, role: String, packaging: String): String = {
    val json = "{\"id\": \"" + id + "\",\"name\":\"" + name + "\",\"provider\":\"" + provider + "\",\"role\":\"" + role + "\",\"packaging\":\"" + packaging + "\"}"
    json
  }

  val riakJSON = getPredefJSON(getUID("pre"), "riak", "chef", "riak", "none")
  val nodejsJSON = getPredefJSON(getUID("pre"), "nodejs", "chef", "nodejs", "npm")
  val playJSON = getPredefJSON(getUID("pre"), "play", "chef", "play", "sbt")
  val akkaJSON = getPredefJSON(getUID("pre"), "akka", "chef", "akka", "sbt")
  val redisJSON = getPredefJSON(getUID("pre"), "redis", "chef", "redis", "none")

}