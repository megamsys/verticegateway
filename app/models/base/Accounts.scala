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
package models.base

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import cache._
import db._
import app.MConfig
import controllers.funnel.FunnelErrors._
import controllers.Constants._

import com.stackmob.scaliak._
import org.megam.common.riak.GunnySack
import org.megam.common.uid.UID
import org.megam.util.Time
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }

/**
 * @author rajthilak
 * authority
 *
 */

case class AccountResult(id: String, first_name: String, last_name: String, phone: String, email: String, api_key: String, password: String, authority: String,  password_reset_key: String, password_reset_sent_at: String, created_at: String) {

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

  //def apply(id: String, email: String, api_key: String, authority: String) = new AccountResult(id, email, api_key, authority, Time.now.toString)
  def apply(id: String, first_name: String, last_name: String, phone: String, email: String, api_key: String, password: String, authority: String, password_reset_key: String, password_reset_sent_at: String) = new AccountResult(id, first_name, last_name, phone, email, api_key, password, authority, password_reset_key, password_reset_sent_at, Time.now.toString)

  def apply(email: String): AccountResult = AccountResult("not found", new String(), new String(), new String(), new String(), email, new String(), new String(), new String(), new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[AccountResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.AccountResultSerialization
    val acctser = new AccountResultSerialization()
    fromJSON(jValue)(acctser.reader)
  }

  def fromJson(json: String): Result[AccountResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue,Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}
case class AccountInput(first_name: String, last_name: String, phone: String, email: String, api_key: String, password: String, authority: String, password_reset_key: String, password_reset_sent_at: String) {
  val json = "{\"first_name\":\"" + first_name + "\",\"last_name\":\"" + last_name + "\",\"phone\":\"" + phone + "\",\"email\":\"" + email + "\",\"api_key\":\"" + api_key + "\",\"password\":\"" + password + "\",\"authority\":\"" + authority + "\",\"password_reset_key\":\"" + password_reset_key + "\",\"password_reset_sent_at\":\"" + password_reset_sent_at + "\"}"
}

case class updateAccountInput(id: String, first_name: String, last_name: String, phone: String, email: String, api_key: String, password: String, authority: String, password_reset_key: String, password_reset_sent_at: String, created_at: String) {
  val json = "{\"id\":\"" + id + "\",\"first_name\":\"" + first_name + "\",\"last_name\":\"" + last_name + "\",\"phone\":\"" + phone + "\",\"email\":\"" + email + "\",\"api_key\":\"" + api_key + "\",\"password\":\"" + password + "\",\"authority\":\"" + authority + "\",\"password_reset_key\":\"" + password_reset_key + "\",\"password_reset_sent_at\":\"" + password_reset_sent_at + "\",\"created_at\":\"" + created_at + "\"}"

}

object Accounts {

  val metadataKey = "ACC"
  val metadataVal = "1002"
  val bindex = "accountId"

  implicit val formats = DefaultFormats

  private val riak = GWRiak("accounts")

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to profile input, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    val accountInput: ValidationNel[Throwable, AccountInput] = (Validation.fromTryCatchThrowable[AccountInput,Throwable] {
      parse(input).extract[AccountInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      m <- accountInput
      bal <- (models.billing.Balances.create(m.email, "{\"credit\":\"0\"}") leftMap { t: NonEmptyList[Throwable] => t })
      uid <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "act").get leftMap { ut: NonEmptyList[Throwable] => ut })
     // org <- models.team.Organizations.create(m.email, OrganizationsInput(DEFAULT_ORG_NAME).json)
    } yield {
      val bvalue = Set(uid.get._1 + uid.get._2)
      val json = AccountResult(uid.get._1 + uid.get._2, m.first_name, m.last_name, m.phone, m.email, m.api_key, m.password, m.authority, m.password_reset_key, m.password_reset_sent_at, Time.now.toString).toJson(false)
      new GunnySack(m.email, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new account item with the 'name' of the item provide as input.
   * A index name account id will point to the "account bucket" bucket.
   */
  def create(input: String): ValidationNel[Throwable, Option[AccountResult]] = {
    (mkGunnySack(input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[AccountResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD,"Account.created success",Console.RESET))
              (parse(gs.get.value).extract[AccountResult].some).successNel[Throwable];
            }
          }
        }
     }
  }

  /**
   * Parse the input body when you start, if its ok, then we process it.
   * Or else send back a bad return code saying "the body contains invalid character, with the message received.
   * If there is an error in the snowflake connection, we need to send one.
   */

  private def updateGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    val ripNel: ValidationNel[Throwable, updateAccountInput] = (Validation.fromTryCatchThrowable[updateAccountInput,Throwable] {
      parse(input).extract[updateAccountInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      rip <- ripNel
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      val bvalue = Set(aor.get.id)
      val json = AccountResult(NilorNot(rip.id, aor.get.id), NilorNot(rip.first_name, aor.get.first_name), NilorNot(rip.last_name, aor.get.last_name), NilorNot(rip.phone, aor.get.phone), NilorNot(rip.email, aor.get.email), NilorNot(rip.api_key, aor.get.api_key), NilorNot(rip.password, aor.get.password), NilorNot(rip.authority, aor.get.authority), NilorNot(rip.password_reset_key, aor.get.password_reset_key), NilorNot(rip.password_reset_sent_at, aor.get.password_reset_sent_at), NilorNot(rip.created_at, aor.get.created_at)).toJson(false)
      new GunnySack((email), json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

 def NilorNot(rip: String, aor: String): String = {
    rip == null match {
      case true => return aor
      case false => return rip
    }
  }


  def updateAccount(email: String, input: String): ValidationNel[Throwable, Option[AccountResult]] = {
    (updateGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[AccountResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD,"Account.updated success",Console.RESET))
              (parse(gs.get.value).extract[AccountResult].some).successNel[Throwable];
            }
          }
        }
    }
  }

  /**
   * Performs a fetch from Riak bucket. If there is an error then ServiceUnavailable is sent back.
   * If not, if there a GunnySack value, then it is parsed. When on parsing error, send back ResourceItemNotFound error.
   * When there is no gunnysack value (None), then return back a failure - ResourceItemNotFound
   */
  def findByEmail(email: String): ValidationNel[Throwable, Option[AccountResult]] = {
    InMemory[ValidationNel[Throwable, Option[AccountResult]]]({
      name: String =>
        {
          play.api.Logger.debug(("%-20s -->[%s]").format("InMemory", email))
          (riak.fetch(email) leftMap { t: NonEmptyList[Throwable] =>
            new ServiceUnavailableError(email, (t.list.map(m => m.getMessage)).mkString("\n"))
          }).toValidationNel.flatMap { xso: Option[GunnySack] =>
            xso match {
              case Some(xs) => {
                (Validation.fromTryCatchThrowable[models.base.AccountResult,Throwable] {
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
      notSed
    }
  }

}
