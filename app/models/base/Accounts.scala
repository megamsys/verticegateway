/*
** Copyright [2013-2016] [Megam Systems]
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
import io.megam.auth.funnel.FunnelErrors._
import models.Constants._
import models.base.Events._

import com.stackmob.scaliak._
import io.megam.auth.stack.AccountResult
import io.megam.common.riak.GunnySack
import io.megam.common.uid.UID
import io.megam.util.Time
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
case class AccountInput(first_name: String, last_name: String, phone: String, email: String, api_key: String, password: String, authority: String, password_reset_key: String, password_reset_sent_at: String) {
  val json = "{\"first_name\":\"" + first_name + "\",\"last_name\":\"" + last_name + "\",\"phone\":\"" + phone + "\",\"email\":\"" + email + "\",\"api_key\":\"" + api_key + "\",\"password\":\"" + password + "\",\"authority\":\"" + authority + "\",\"password_reset_key\":\"" + password_reset_key + "\",\"password_reset_sent_at\":\"" + password_reset_sent_at + "\"}"
}

case class AccountWrapper(s: AccountResult) {
  val askForReset = s.password_reset_key != null && s.password_reset_key.trim.length > 0
}

object Accounts {

  implicit val formats = DefaultFormats

  private lazy val bucker = "accounts"

  private lazy val idxedBy = idxAccountsId

  private val riak = GWRiak(bucker)

  private def pubEvent(accounts_id: String, action: String, evtMap: Map[String, String]) = Events(accounts_id, EVENTUSER, action, evtMap).createAndPub()

  private def accountNel(input: String): ValidationNel[Throwable, AccountInput] = {
    (Validation.fromTryCatchThrowable[AccountInput, Throwable] {
      parse(input).extract[AccountInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
  }

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to profile input, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(input: String): ValidationNel[Throwable, Option[(GunnySack, AccountResult)]] = {
    for {
      m <- accountNel(input)
      uid <- (UID("act").get leftMap { ut: NonEmptyList[Throwable] => ut })
      // org <- models.team.Organizations.create(m.email, OrganizationsInput(DEFAULT_ORG_NAME).json)
    } yield {
      val bvalue = Set(uid.get._1 + uid.get._2)
      val are = AccountResult(uid.get._1 + uid.get._2, m.first_name, m.last_name, m.phone, m.email, m.api_key, m.password, m.authority, m.password_reset_key, m.password_reset_sent_at, Time.now.toString())
      (new GunnySack(m.email, are.toJson(false), RiakConstants.CTYPE_TEXT_UTF8, None,
        Map.empty, Map((idxedBy, bvalue))), are).some
    }
  }

  private def updGunnySack(email: String, input: String): ValidationNel[Throwable, Option[(GunnySack, AccountResult)]] = {
    for {
      m <- accountNel(input)
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      val bvalue = Set(aor.get.id)
      val are = AccountResult(aor.get.id, swap(m.first_name, aor.get.first_name),
        swap(m.last_name, aor.get.last_name), swap(m.phone, aor.get.phone),
        swap(m.email, aor.get.email), swap(m.api_key, aor.get.api_key),
        swap(m.password, aor.get.password), swap(m.authority, aor.get.authority),
        swap(m.password_reset_key, aor.get.password_reset_key),
        swap(m.password_reset_sent_at, aor.get.password_reset_sent_at),
        Time.now.toString)
      (new GunnySack(email, are.toJson(false), RiakConstants.CTYPE_TEXT_UTF8, None,
        Map.empty, Map((idxedBy, bvalue))), are).some
    }
  }

  //create request from input
  def create(input: String): ValidationNel[Throwable, Option[AccountResult]] = {
    for {
      gae <- mkGunnySack(input)
      ogsr <- (riak.store(gae.get._1) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      pubEvent(gae.get._2.id, Events.CREATE, Map(EVTEMAIL -> gae.get._2.email)) flatMap { x =>
        (play.api.Logger.warn(("%s%s%-20s%s").format(Console.BLUE, Console.BOLD, "NSQ.published success", Console.RESET))).successNel[Throwable]
      } //we ignore errors
      play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Account.created success", Console.RESET))
      gae.get._2.some
    }
  }

  def update(email: String, input: String): ValidationNel[Throwable, Option[AccountResult]] = {
    for {
      gae <- updGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] => err }
      ogsr <- riak.store(gae.get._1) leftMap { t: NonEmptyList[Throwable] => t }
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Account.updated success", Console.RESET))
      if (new AccountWrapper(gae.get._2).askForReset) {
        pubEvent(gae.get._2.id, Events.RESET, Map(EVTEMAIL -> gae.get._2.email,
          EVTCLICK -> ("reset?email=" + gae.get._2.email + "&resettoken=" + gae.get._2.password_reset_key))) flatMap { x =>
          (play.api.Logger.warn(("%s%s%-20s%s").format(Console.BLUE, Console.BOLD, "NSQ.published success", Console.RESET))).successNel[Throwable]
        } //we ignore errors
      }
      gae.get._2.some
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
                (Validation.fromTryCatchThrowable[io.megam.auth.stack.AccountResult, Throwable] {
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
    val fetchValue = riak.fetchIndexByValue(new GunnySack(idxedBy, id,
      RiakConstants.CTYPE_TEXT_UTF8, None, Map.empty, Map(("", Set("")))))
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

  private def swap(old: String, aor: String): String = {
    old == null match {
      case true => return aor
      case false => return old
    }
  }
}
