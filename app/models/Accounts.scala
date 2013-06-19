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
package models

import scalaz._
import Scalaz._
import scalaz.NonEmptyList._
import play.api._
import play.api.mvc._
import net.liftweb.json._
import controllers.stack.MConfig
import com.stackmob.scaliak._
import com.basho.riak.client.query.indexes.{ RiakIndexes, IntIndex, BinIndex }
import com.basho.riak.client.http.util.{ Constants => RiakConstants }
import models._
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import org.megam.common.uid._
import net.liftweb.json.scalaz.JsonScalaz.field
/**
 * @author rajthilak
 * authority
 */

case class AccountResult(id: String, email: String, api_key: String, authority: String)
case class AccountInput(email: String, api_key: String, authority: String) {
  val getAccountJson = "\"email\":\"" + email + "\",\"api_key\":\"" + api_key + "\",\"authority\":\"" + authority + "\""
}
object Accounts {

  implicit val formats = DefaultFormats
  private val SFHOST = MConfig.snowflakeHost
  private val SFPORT: Int = MConfig.snowflakePort
  private lazy val riak: GSRiak = GSRiak(MConfig.riakurl, "accounts")

  def create(input: String): ValidationNel[Error, Option[AccountResult]] = {
    val id = UID(SFHOST, SFPORT, "act").get
    val res: UniqueID = id match {
      case Success(uid) => {
        println("------>" + uid)
        uid
      }
      case Failure(error) => { None }
    }
    val metadataKey = "Field"
    val metadataVal = "1002"
    val inputJson = parse(input)
    val m = inputJson.extract[AccountInput]
    val bindex = BinIndex.named("accountId")
    val bvalue = Set(res.get._1 + res.get._2)
    val json = "{\"id\": \"" + (res.get._1 + res.get._2) + "\"," + m.getAccountJson + "}"
    val storeValue = riak.store(new GunnySack(m.email, json, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))
    storeValue match {
      case Success(msg) => Validation.success[Error, Option[AccountResult]](None).toValidationNel
      case Failure(err) => Validation.failure[Error, Option[AccountResult]](new Error("Account.create Not Implemented")).toValidationNel
    }
  }

  def findByEmail(email: String): ValidationNel[Error, Option[AccountResult]] = {
    //extract the json into ValidationNel
    riak.fetch(email) match {
      case Success(msg) => {
        val caseValue = msg.get
        val json = parse(caseValue.value)
        val m = json.extract[AccountResult]
        println("some value ---------" + m)
        Validation.success[Error, Option[AccountResult]](Some(m)).toValidationNel
        // parse(msg.value).extract[AccountResult]  
      }
      case Failure(err) => Validation.failure[Error, Option[AccountResult]](new Error("Your account is not registered, pls register your account.")).toValidationNel
    }
  }

  /**
   * Index on email
   */
  def findById(id: String): ValidationNel[Error, Option[AccountResult]] = {
    val metadataKey = "Field"
    val metadataVal = "1002"
    val bindex = BinIndex.named("")
    val bvalue = Set("")
    val fetchValue = riak.fetchIndexByValue(new GunnySack("accountId", id, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))

    fetchValue match {
      case Success(msg) => {
        val key = msg match {
          case List(x) => x
        }
        findByEmail(key)
      }
      case Failure(err) => Validation.failure[Error, Option[AccountResult]](new Error("Email is not already exists")).toValidationNel
    }
  }

  //def freq[T](seq: List[T]) = seq.groupBy(x => x).mapValues(_.length)
}