/* 
** Copyright [2012] [Megam Systems]
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
  val key = "mykey3"
  val HMAC_HEADER = "X-Megam-HMAC"

  def getUID(agent: String): String = {
    val uid = UID(SFHOST, SFPORT, agent)
    val id = uid.get
    val res: UniqueID = id match {
      case Success(uid) => {
        uid
      }
      case Failure(error) => { 
        Logger.info("""
            |
            |The id creation was failed from snowflake server 
            |If this error persits, ask for help on the forums.""".stripMargin 
      + "\n" + apiAccessed)
      }
    }
    (res.get._1 + res.get._2)
  }

  def getAccountID(sentHmacHeader: Option[String]): String = {
    val email: String = sentHmacHeader match {
      case Some(x) if x.contains(":") && x.split(":").length == 2 => {
        val headerParts = x.split(":")
        headerParts(0)
      }
      case _ => ""
    }
    val id = models.Accounts.findByEmail(email) match {
      case Success(optAcc) => {
        val foundAccount = optAcc.get
        foundAccount.id
      }
      case Failure(err) => 
         Logger.info("""
            | Your account is doesn't exists in megam.co.
            | Please register your account in megam.co. After then you can use megam.co high available facilities""")
    }
    id
  }
  
  def getPredefJSON(id: String, name: String, provider: String, role: String, packaging: String): String = {
  
        val json = "{\"id\": \"" + id + "\",\"name\":\"" + name + "\",\"provider\":\"" + provider + "\",\"role\":\"" + role + "\",\"packaging\":\"" + packaging + "\"}"             
      json
  }
  
  val riakJSON = getPredefJSON(getUID("pre"), "riak", "chef", "riak", "")
  val nodejsJSON = getPredefJSON(getUID("pre"), "nodejs", "chef", "nodejs", "")
  val playJSON = getPredefJSON(getUID("pre"), "play", "chef", "play", "sbt")
  val akkaJSON = getPredefJSON(getUID("pre"), "akka", "chef", "akka", "sbt")
  val redisJSON = getPredefJSON(getUID("pre"), "redis", "chef", "redis", "")   
  
}