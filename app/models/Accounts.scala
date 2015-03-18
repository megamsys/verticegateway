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
package models

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
//import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import com.stackmob.scaliak._
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import org.megam.util.Time
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import models.cache._
import models.riak._
import controllers.funnel.FunnelErrors._
import controllers.Constants._
import controllers.stack.MConfig

/**
 * @author rajthilak
 * authority
 */

case class AccountResult(id: String, email: String, api_key: String, authority: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.AccountResultSerialization
    val acctser = new AccountResultSerialization()
    toJSON(this)(acctser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }

}

object AccountResult {

  def apply(id: String, email: String, api_key: String, authority: String) = new AccountResult(id, email, api_key, authority, Time.now.toString)

  def apply(email: String): AccountResult = AccountResult("not found", email, new String(), new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[AccountResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.AccountResultSerialization
    val acctser = new AccountResultSerialization()
    fromJSON(jValue)(acctser.reader)
  }

  def fromJson(json: String): Result[AccountResult] = (Validation.fromTryCatch[net.liftweb.json.JValue] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}
case class AccountInput(email: String, api_key: String, authority: String) {
  val json = "{\"email\":\"" + email + "\",\"api_key\":\"" + api_key + "\",\"authority\":\"" + authority + "\"}"
}
object Accounts {

  implicit val formats = DefaultFormats

  private val riak = GWRiak("accounts")
  /**
   * Parse the input body when you start, if its ok, then we process it.
   * Or else send back a bad return code saying "the body contains invalid character, with the message received.
   * If there is an error in the snowflake connection, we need to send one.
   */
  def create(input: String): ValidationNel[Throwable, Option[AccountResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Accounts", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("input json", input))
    (Validation.fromTryCatch[models.AccountInput] {
      parse(input).extract[AccountInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage)
    }).toValidationNel.flatMap { m: AccountInput =>
      play.api.Logger.debug(("%-20s -->[%s]").format("AccountInput", m))
      UID(MConfig.snowflakeHost, MConfig.snowflakePort, "act").get match {
        case Success(uid) => {
          val metadataKey = "Field"
          val metadataVal = "1002"
          val bindex = "accountId"
          val bvalue = Set(uid.get._1 + uid.get._2)
          val acctRes = AccountResult(uid.get._1 + uid.get._2, m.email, m.api_key, m.authority)
          play.api.Logger.debug(("%-20s -->[%s]").format("json with uid", acctRes.toJson(false)))          
          val storeValue = riak.store(new GunnySack(m.email, acctRes.toJson(false), RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))
          storeValue match {
            case Success(succ) => acctRes.some.successNel[Throwable]
            case Failure(err) => Validation.failure[Throwable, Option[AccountResult]](
              new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))).toValidationNel
          }
        }
        case Failure(err) => Validation.failure[Throwable, Option[AccountResult]](
          new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))).toValidationNel
      }
    }

  }

  
  /**
   * Performs a fetch from Riak bucket. If there is an error then ServiceUnavailable is sent back.
   * If not, if there a GunnySack value, then it is parsed. When on parsing error, send back ResourceItemNotFound error.
   * When there is no gunnysack value (None), then return back a failure - ResourceItemNotFound
   */
  def findByEmail(email: String): ValidationNel[Throwable, Option[AccountResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Accounts", "findByEmail:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("findByEmail", email))
    InMemory[ValidationNel[Throwable, Option[AccountResult]]]({
      name: String =>
        {
          play.api.Logger.debug(("%-20s -->[%s]").format("InMemory", email))
          (riak.fetch(email) leftMap { t: NonEmptyList[Throwable] =>
            new ServiceUnavailableError(email, (t.list.map(m => m.getMessage)).mkString("\n"))
          }).toValidationNel.flatMap { xso: Option[GunnySack] =>
            xso match {
              case Some(xs) => {
                (Validation.fromTryCatch[models.AccountResult] {
                //  initiate_default_cloud(email)
                  parse(xs.value).extract[AccountResult]
                } leftMap { t: Throwable =>
                  new ResourceItemNotFound(email, t.getMessage)
                }).toValidationNel.flatMap { j: AccountResult =>
                  Validation.success[Throwable, Option[AccountResult]](j.some).toValidationNel
                }
              }
              case None => Validation.failure[Throwable, Option[AccountResult]](new ResourceItemNotFound(email, "")).toValidationNel
            }
          }
        }
    }).get(email).eval(InMemoryCache[ValidationNel[Throwable, Option[AccountResult]]]())

  }

  /**
   * Find by the accounts id.
   */
  def findByAccountsId(id: String): ValidationNel[Throwable, Option[AccountResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Accounts", "findByAccountsId:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("accounts id", id))
    val metadataKey = "Field"
    val metadataVal = "1002"
    val bindex = ""
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
      case Failure(err) => Validation.failure[Throwable, Option[AccountResult]](
        new ServiceUnavailableError(id, (err.list.map(m => m.getMessage)).mkString("\n"))).toValidationNel
    }
  }

  implicit val sedimentAccountEmail = new Sedimenter[ValidationNel[Throwable, Option[AccountResult]]] {
    def sediment(maybeASediment: ValidationNel[Throwable, Option[AccountResult]]): Boolean = {
      val notSed = maybeASediment.isSuccess
      play.api.Logger.debug("%-20s -->[%s]".format("|^/^|-->ACT:sediment:", notSed))
      notSed
    }
  }

}