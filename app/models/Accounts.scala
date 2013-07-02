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
import play.api.Logger
/**
 * @author rajthilak
 * authority
 */

case class AccountResult(id: String, email: String, api_key: String, authority: String) {
  val empty: AccountResult = this
  def this() = this(new String(), new String(), new String(), new String())
  override def toString = "\"id\":\"" + id + "\",\"email\":\"" + email + "\",\"api_key\":\"" + api_key + "\""
}
case class AccountInput(email: String, api_key: String, authority: String) {
  val json = "\"email\":\"" + email + "\",\"api_key\":\"" + api_key + "\",\"authority\":\"" + authority + "\""
}
object Accounts {

  implicit val formats = DefaultFormats

  private lazy val riak: GSRiak = GSRiak(MConfig.riakurl, "accounts")
  /**
   * Parse the input body when you start, if its ok, then we process it.
   * Or else send back a bad return code saying "the body contains invalid character, with the message received.
   * If there is an error in the snowflake connection, we need to send one.
   */
  def create(input: String): ValidationNel[Error, Option[AccountResult]] = {
    Logger.debug("models.Account create: entry\n" + input)
    (Validation.fromTryCatch {
      parse(input).extract[AccountInput]
    } leftMap { t: Throwable =>
      new Error(
        """Body sent contains invalid input. 'body:'  '%s' 
            |
            |The error received when parsing the JSON is 
            |=====>\n
            |%s
            |=====<
            |Verify the body content as required for this resource. 
            |Read https://api.megam.co, http://docs.megam.co for more help. Ask for help on the forums.""".
          format(input, t.getMessage).stripMargin)
    }).toValidationNel.flatMap { m: AccountInput =>
      UID(MConfig.snowflakeHost, MConfig.snowflakePort, "act").get match {
        case Success(uid) => {
          val metadataKey = "Field"
          val metadataVal = "1002"
          val bindex = BinIndex.named("accountId")
          val bvalue = Set(uid.get._1 + uid.get._2)
          val json = "{\"id\": \"" + (uid.get._1 + uid.get._2) + "\"," + m.json + "}"
          val storeValue = riak.store(new GunnySack(m.email, json, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))
          storeValue match {
            case Success(succ) => Validation.success[Error, Option[AccountResult]] {
              (parse(succ.getOrElse(new GunnySack()).value).extract[AccountResult].some)
            }.toValidationNel
            case Failure(err) => Validation.failure[Error, Option[AccountResult]](
              new Error(
                """Account creation failed for 'email:' with api_key : '%s'
            |
            |This appears to be a problem that occurred while connecting to our datasource. We humbly request you to 
            |try the same again. If it still persists after reading our doc, retrying, please contact our support.
            |Read https://api.megam.co, http://docs.megam.co for more help. Ask for help on the forums.""".format("none:?").stripMargin)).toValidationNel
          }
        }
        case Failure(err) => Validation.failure[Error, Option[AccountResult]](
          new Error(
            """Account creation failed for 'email:' with api_key : '%s'
            |
            |This appears to be a problem with the unique id creation. We humbly request you to 
            |try the same again.  If it still persists after reading our doc, retrying, please contact our support.
            |Read https://api.megam.co, http://docs.megam.co for more help. Ask for help on the forums.""".format("none:?").stripMargin)).toValidationNel
      }
    }

  }

  def findByEmail(email: String): ValidationNel[Error, Option[AccountResult]] = {
    Logger.debug("models.Account findByEmail: entry:" + email)
    riak.fetch(email) match {
      case Success(succ) => {
        Logger.debug("models.Account findByEmail: Found:" + succ)
        (Validation.fromTryCatch {
          parse(succ.getOrElse(new GunnySack()).value).extract[AccountResult]
        } leftMap { t: Throwable =>
          new Error(
            """Stored Account contains invalid json 'json:'  '%s' 
            |
            |The error received when parsing the JSON is 
            |=====>\n
            |%s
            |=====<
            |Verify the content as required for this resource. 
            |Have you registered for an api_key with us ? We humbly request you to  
            |try the same request again.  If it still persists after reading our doc, retrying, please contact our support.
            |Read https://api.megam.co, http://docs.megam.co for more help. Ask for help on the forums.""".
              format(succ.getOrElse(new GunnySack()).value, t.getMessage).stripMargin)
        }).toValidationNel.flatMap { j: AccountResult =>
          Validation.success[Error, Option[AccountResult]](j.some).toValidationNel
        }
      }
      case Failure(err) => Validation.failure[Error, Option[AccountResult]](
        new Error(
          """Account doesn't exist for 'email:' '%s'
            |
            |Have you registered for an api_key with us ? We humbly request you to  
            |try the same request again.  If it still persists after reading our doc, retrying, please contact our support.
            |Read https://api.megam.co, http://docs.megam.co for more help. Ask for help on the forums.""".format(email).stripMargin)).toValidationNel

    }
  }

  /**
   * Index on email
   */
  def findById(id: String): ValidationNel[Error, Option[AccountResult]] = {
    Logger.debug("models.Account findById: entry:" + id)

    val metadataKey = "Field"
    val metadataVal = "1002"
    val bindex = BinIndex.named("")
    val bvalue = Set("")
    val fetchValue = riak.fetchIndexByValue(new GunnySack("accountId", id,
      RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))

    fetchValue match {
      case Success(msg) => {
        val key = msg match {
          case List(x) => x
        }
        findByEmail(key)
      }
      case Failure(err) => Validation.failure[Error, Option[AccountResult]](new Error("""
            | Your account doesn't exists in megam.co.
            | Please register your account in megam.co. After then you can use megam.co high available facilities""")).toValidationNel
    }
  }

}