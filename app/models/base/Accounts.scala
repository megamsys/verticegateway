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
import controllers.Constants._

import io.megam.auth.stack.AccountResult
import io.megam.common.uid.UID
import io.megam.util.Time
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

import java.util.UUID
import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.annotation.tailrec

import models.team._
import models.base.Events._

/**
 * @author rajthilak
 * authority
 *
 */

case class AccountInput(first_name: String, last_name: String, phone: String, email: String, api_key: String, password: String, authority: String, password_reset_key: String, password_reset_sent_at: String) {
  val json = "{\"first_name\":\"" + first_name + "\",\"last_name\":\"" + last_name + "\",\"phone\":\"" + phone + "\",\"email\":\"" + email + "\",\"api_key\":\"" + api_key + "\",\"password\":\"" + password + "\",\"authority\":\"" + authority + "\",\"password_reset_key\":\"" + password_reset_key + "\",\"password_reset_sent_at\":\"" + password_reset_sent_at + "\"}"
}

case class AccountResetSack(password_reset_key: String, password_reset_sent_at: String) {
  val json = "{\"id\":\"\",\"first_name\":\"\",\"last_name\":\"\",\"phone\":\"\",\"email\":\"\",\"api_key\":\"\",\"password\":\"\",\"authority\":\"\",\"password_reset_key\":\"" + password_reset_key + "\",\"password_reset_sent_at\":\"" + password_reset_sent_at + "\",\"created_at\":\"\"}"
}

sealed class AccountSacks extends CassandraTable[AccountSacks, AccountResult] {
  //object id extends  UUIDColumn(this) with PartitionKey[UUID] {
  //  override lazy val name = "id"
  //}
  object id extends StringColumn(this)
  object first_name extends StringColumn(this)
  object last_name extends StringColumn(this)
  object phone extends StringColumn(this)
  object email extends StringColumn(this) with PrimaryKey[String]
  object api_key extends StringColumn(this)
  object password extends StringColumn(this)
  object authority extends StringColumn(this)
  object password_reset_key extends StringColumn(this)
  object password_reset_sent_at extends StringColumn(this)
  //object json_claz extends StringColumn(this)
  object created_at extends StringColumn(this)

  def fromRow(row: Row): AccountResult = {
    AccountResult(
      id(row),
      first_name(row),
      last_name(row),
      phone(row),
      email(row),
      api_key(row),
      password(row),
      authority(row),
      password_reset_key(row),
      password_reset_sent_at(row),
      // json_claz(row),
      created_at(row))
  }
}

abstract class ConcreteAccounts extends AccountSacks with RootConnector {
  // you can even rename the table in the schema to whatever you like.
  override lazy val tableName = "accounts"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(account: AccountResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, account.id)
      .value(_.first_name, NilorNot(account.first_name, ""))
      .value(_.last_name, NilorNot(account.last_name, ""))
      .value(_.phone, NilorNot(account.phone, ""))
      .value(_.email, NilorNot(account.email, ""))
      .value(_.api_key, NilorNot(account.api_key, ""))
      .value(_.password, NilorNot(account.password, ""))
      .value(_.authority, NilorNot(account.authority, ""))
      .value(_.password_reset_key, NilorNot(account.password_reset_key, ""))
      .value(_.password_reset_sent_at, NilorNot(account.password_reset_sent_at, ""))
      // .value(_.json_claz, account.json_claz)
      .value(_.created_at, account.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def getRecord(email: String): ValidationNel[Throwable, Option[AccountResult]] = {
    val res = select.where(_.email eqs email).one()
    Await.result(res, 5.seconds).successNel
  }

  def updateRecord(email: String, rip: AccountResult, aor: Option[AccountResult]): ValidationNel[Throwable, ResultSet] = {

    val res = update.where(_.email eqs NilorNot(email, aor.get.email))
      .modify(_.id setTo NilorNot(rip.id, aor.get.id))
      .and(_.first_name setTo NilorNot(rip.first_name, aor.get.first_name))
      .and(_.last_name setTo NilorNot(rip.last_name, aor.get.last_name))
      .and(_.phone setTo NilorNot(rip.phone, aor.get.phone))
      .and(_.api_key setTo NilorNot(rip.api_key, aor.get.api_key))
      .and(_.password setTo NilorNot(rip.password, aor.get.password))
      .and(_.authority setTo NilorNot(rip.authority, aor.get.authority))
      .and(_.password_reset_key setTo NilorNot(rip.password_reset_key, aor.get.password_reset_key))
      .and(_.password_reset_sent_at setTo NilorNot(rip.password_reset_sent_at, aor.get.password_reset_sent_at))
      // .and(_.json_claz setTo NilorNot(rip.json_claz, aor.get.json_claz))
      .and(_.created_at setTo NilorNot(rip.created_at, aor.get.created_at))
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def NilorNot(rip: String, aor: String): String = {
    rip == null || rip == "" match {
      case true => return aor
      case false => return rip
    }
  }

}

object Accounts extends ConcreteAccounts {

  implicit val formats = DefaultFormats

  private def parseAccountInput(input: String): ValidationNel[Throwable, AccountInput] = {
    (Validation.fromTryCatchThrowable[AccountInput, Throwable] {
      parse(input).extract[AccountInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
  }

  private def generateAccountSet(id: String, m: AccountInput): ValidationNel[Throwable, AccountResult] = {
    (Validation.fromTryCatchThrowable[AccountResult, Throwable] {
      AccountResult(id, m.first_name, m.last_name, m.phone, m.email, m.api_key, m.password, m.authority, m.password_reset_key, m.password_reset_sent_at, Time.now.toString)
    } leftMap { t: Throwable => new MalformedBodyError(m.json, t.getMessage) }).toValidationNel
  }

  private def orgChecker(email: String, orgs: Seq[OrganizationsResult], acc: AccountResult): ValidationNel[Throwable, AccountResult] = {
    val org_json = "{\"name\":\"" + DEFAULT_ORG_NAME + "\"}"
    val domain_json = "{\"name\":\"" + DEFAULT_DOMAIN_NAME + "\"}"
    if (!orgs.isEmpty)
      return Validation.success[Throwable, AccountResult](acc).toValidationNel
    else {
      (models.team.Organizations.create(email, org_json.toString) leftMap { t: NonEmptyList[Throwable] =>
        new ServiceUnavailableError(email, (t.list.map(m => m.getMessage)).mkString("\n"))
      }).toValidationNel.flatMap { xso: Option[OrganizationsResult] =>
        xso match {
          case Some(xs) => {
            (models.team.Domains.create(xs.id, domain_json.toString) leftMap { t: NonEmptyList[Throwable] =>
              new ServiceUnavailableError(xs.id, (t.list.map(m => m.getMessage)).mkString("\n"))
            }).toValidationNel.flatMap { dso: DomainsResult =>
              Validation.success[Throwable, AccountResult](acc).toValidationNel
            }
          }
          case None => Validation.success[Throwable, AccountResult](acc).toValidationNel
        }
      }
    }
  }

  def create(input: String): ValidationNel[Throwable, AccountResult] = {
    for {
      m <- parseAccountInput(input)
      uir <- (UID("act").get leftMap { ut: NonEmptyList[Throwable] => ut })
      acc <- generateAccountSet(uir.get._1 + uir.get._2, m)
      set <- insertNewRecord(acc)
      orgs <- models.team.Organizations.findByEmail(m.email)
      res <- orgChecker(m.email, orgs, acc)
      evn <- Events(acc.id, EVENTUSER, Events.ONBOARD, Map(EVTEMAIL -> acc.email)).createAndPub()
    } yield {
      res
    }
  }

  def update(email: String, input: String): ValidationNel[Throwable, Option[AccountResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("Update Account", email))
    val ripNel: ValidationNel[Throwable, AccountResult] = (Validation.fromTryCatchThrowable[AccountResult, Throwable] {
      parse(input).extract[AccountResult]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
    for {
      rip <- ripNel
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      set <- updateRecord(email, rip, aor)
    } yield {
      aor
    }
  }

  def reset(email: String): ValidationNel[Throwable, Option[AccountResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("Reset Account", email))
    val hex = randomAlphaNumericString(16)
    for {
      rip <- update(email, AccountResetSack(hex, Time.now.toString).json)
      acc <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      evn <- Events(acc.get.id, EVENTUSER, Events.RESET, Map(EVTEMAIL -> acc.get.email, EVTTOKEN -> hex)).createAndPub()
    } yield {
      rip
    }
  }

   
  def repassword(input: String): ValidationNel[Throwable, Option[AccountResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("Repassword Account", email))
    val ripNel: ValidationNel[Throwable, AccountResult] = (Validation.fromTryCatchThrowable[AccountResult, Throwable] {
      parse(input).extract[AccountResult]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      rip <- ripNel
      acc <- (Accounts.findByEmail(rip.email) leftMap { t: NonEmptyList[Throwable] => t })
      chk <- (verifytoken(rip, acc.get) leftMap { t: NonEmptyList[Throwable] => t })
      upd <- (updateRecord(rip.email, rip, acc) leftMap { t: NonEmptyList[Throwable] => t})
    } yield {
      acc
    }
  }
  
  def verifytoken(update_account: AccountResult, old_account: AccountResult): ValidationNel[Throwable, AccountResult] = {
    if (update_account.password_reset_key == old_account.password_reset_key) {
      Validation.success[Throwable, AccountResult](update_account).toValidationNel
    } else {
      Validation.failure[Throwable, AccountResult](new MalformedBodyError(update_account.email, "")).toValidationNel
    }
  }

  /**
   * Performs a fetch from scylladb. If there is an error then ServiceUnavailable is sent back.
   * If not, if there a option value, then it is parsed. When on parsing error, send back ResourceItemNotFound error.
   * When there is option value (None), then return back a failure - ResourceItemNotFound
   */
  def findByEmail(email: String): ValidationNel[Throwable, Option[AccountResult]] = {
    InMemory[ValidationNel[Throwable, Option[AccountResult]]]({
      name: String =>
        {
          play.api.Logger.debug(("%-20s -->[%s]").format("InMemory", email))
          (getRecord(email) leftMap { t: NonEmptyList[Throwable] =>
            new ServiceUnavailableError(email, (t.list.map(m => m.getMessage)).mkString("\n"))
          }).toValidationNel.flatMap { xso: Option[AccountResult] =>
            xso match {
              case Some(xs) => {
                Validation.success[Throwable, Option[AccountResult]](xs.some).toValidationNel
              }
              case None => Validation.failure[Throwable, Option[AccountResult]](new ResourceItemNotFound(email, "")).toValidationNel
            }
          }
        }
    }).get(email).eval(InMemoryCache[ValidationNel[Throwable, Option[AccountResult]]]())

  }

  implicit val sedimentAccountEmail = new Sedimenter[ValidationNel[Throwable, Option[AccountResult]]] {
    def sediment(maybeASediment: ValidationNel[Throwable, Option[AccountResult]]): Boolean = {
      val notSed = maybeASediment.isSuccess
      notSed
    }
  }

  def randomAlphaNumericString(length: Int): String = {
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    randomStringFromCharList(length, chars)
  }

  def randomStringFromCharList(length: Int, chars: Seq[Char]): String = {
    val sb = new StringBuilder
    for (i <- 1 to length) {
      val randomNum = util.Random.nextInt(chars.length)
      sb.append(chars(randomNum))
    }
    sb.toString
  }

}
