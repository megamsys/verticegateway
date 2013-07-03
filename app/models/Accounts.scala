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
import play.api.Logger

import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz.field

import com.stackmob.scaliak._
import com.basho.riak.client.query.indexes.{ RiakIndexes, IntIndex, BinIndex }
import com.basho.riak.client.http.util.{ Constants => RiakConstants }

import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import org.megam.common.uid._
import controllers.stack.stack._
import controllers.stack.MConfig
import models._
/**
 * @author rajthilak
 * authority
 */

case class AccountResult(id: String, email: String, api_key: String, authority: String) {
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
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage)
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
              new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))).toValidationNel
          }
        }
        case Failure(err) => Validation.failure[Error, Option[AccountResult]](
          new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))).toValidationNel
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
          new ResourceItemNotFound(email, t.getMessage)
        }).toValidationNel.flatMap { j: AccountResult =>
          Validation.success[Error, Option[AccountResult]](j.some).toValidationNel
        }
      }
      case Failure(err) => Validation.failure[Error, Option[AccountResult]](
        new ServiceUnavailableError(email, (err.list.map(m => m.getMessage)).mkString("\n"))).toValidationNel
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
      case Failure(err) => Validation.failure[Error, Option[AccountResult]](
        new ServiceUnavailableError(id, (err.list.map(m => m.getMessage)).mkString("\n"))).toValidationNel
    }
  }

}