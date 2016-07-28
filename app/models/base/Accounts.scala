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
import io.megam.auth.stack.{ Name, Phone, Password, States, Approval, Dates, Suspend }
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

case class AccountInput(name: Name, phone: Phone, email: String, api_key: String, password: Password, states: States, approval: Approval, suspend: Suspend, registration_ip_address: String,  dates: Dates) {
  val json = "{\"name\":" +name.json+",\"phone\":" + phone.json + ",\"email\":\"" + email + "\",\"api_key\":\"" + api_key + "\",\"password\":" + password.json + ",\"states\":" + states.json + ",\"approval\":" + approval.json + ",\"suspend\":" + suspend.json + ",\"registration_ip_address\":\"" + registration_ip_address + "\",\"dates\":" + dates.json + "}"
}

case class AccountResetSack(password_reset_key: String, password_reset_sent_at: String) {
  val pass = Password(password_reset_key, password_reset_sent_at)
  val json = "{\"id\":\"\",\"name\":\"\",\"phone\":\"\",\"email\":\"\",\"api_key\":\"\",\"password\":\"" + pass.json + "\",\"states\":\"\",\"approval\":\"\",\"suspend\":\"\",,\"registration_ip_address\":\"\",\"dates\":\"\"}"
}

sealed class AccountSacks extends CassandraTable[AccountSacks, AccountResult] {
  //object id extends  UUIDColumn(this) with PartitionKey[UUID] {
  //  override lazy val name = "id"
  //}
  implicit val formats = DefaultFormats
  object id extends StringColumn(this)

  object name extends JsonColumn[AccountSacks, AccountResult, Name](this) {
    override def fromJson(obj: String): Name = {
      JsonParser.parse(obj).
      extract[Name]
    }

    override def toJson(obj: Name): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object phone extends JsonColumn[AccountSacks, AccountResult, Phone](this) {
    override def fromJson(obj: String): Phone = {
      JsonParser.parse(obj).extract[Phone]
    }

    override def toJson(obj: Phone): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object email extends StringColumn(this) with PrimaryKey[String]

  object api_key extends StringColumn(this)

  object password extends JsonColumn[AccountSacks, AccountResult, Password](this) {
    override def fromJson(obj: String): Password = {
      JsonParser.parse(obj).extract[Password]
    }

    override def toJson(obj: Password): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object states extends JsonColumn[AccountSacks, AccountResult, States](this) {
    override def fromJson(obj: String): States = {
      JsonParser.parse(obj).extract[States]
    }

    override def toJson(obj: States): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object approval extends JsonColumn[AccountSacks, AccountResult, Approval](this) {
    override def fromJson(obj: String): Approval = {
      JsonParser.parse(obj).extract[Approval]
    }

    override def toJson(obj: Approval): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object suspend extends JsonColumn[AccountSacks, AccountResult, Suspend](this) {
    override def fromJson(obj: String): Suspend = {
      JsonParser.parse(obj).extract[Suspend]
    }

    override def toJson(obj: Suspend): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object registration_ip_address extends StringColumn(this)
  object dates extends JsonColumn[AccountSacks, AccountResult, Dates](this) {
    override def fromJson(obj: String): Dates = {
      JsonParser.parse(obj).extract[Dates]
    }

    override def toJson(obj: Dates): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  def fromRow(row: Row): AccountResult = {
    AccountResult(
      id(row),
      name(row),
      phone(row),
      email(row),
      api_key(row),
      password(row),
      states(row),
      approval(row),
      suspend(row),
      registration_ip_address(row),
      dates(row))
  }
}

abstract class ConcreteAccounts extends AccountSacks with RootConnector {
  // you can even rename the table in the schema to whatever you like.
  override lazy val tableName = "accounts"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(account: AccountResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, account.id)
      .value(_.name, account.name)
     .value(_.phone, account.phone)
      .value(_.email, NilorNot(account.email, ""))
      .value(_.api_key, NilorNot(account.api_key, ""))
      .value(_.password, account.password)
      .value(_.states, account.states)
      .value(_.approval, account.approval)
      .value(_.suspend, account.suspend)
      .value(_.registration_ip_address, NilorNot(account.registration_ip_address, ""))
      // .value(_.json_claz, account.json_claz)
      .value(_.dates, account.dates)
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
      .and(_.name setTo new Name(NilorNot(rip.name.first_name, aor.get.name.first_name),
         NilorNot(rip.name.last_name, aor.get.name.last_name)))


       .and(_.phone setTo new Phone(NilorNot(rip.phone.phone, aor.get.phone.phone),
       NilorNot(rip.phone.phone_verified, aor.get.phone.phone_verified)))

      .and(_.api_key setTo NilorNot(rip.api_key, aor.get.api_key))

      .and(_.password setTo new Password(NilorNot(rip.password.password, aor.get.password.password),
        NilorNot(rip.password.password_reset_key, aor.get.password.password_reset_key),
        NilorNot(rip.password.password_reset_sent_at, aor.get.password.password_reset_sent_at)))

       .and(_.states setTo new States(NilorNot(rip.states.authority, aor.get.states.authority),
         NilorNot(rip.states.active, aor.get.states.active),
         NilorNot(rip.states.blocked, aor.get.states.blocked),
         NilorNot(rip.states.staged, aor.get.states.staged)))

    .and(_.approval setTo new Approval(NilorNot(rip.approval.approved, aor.get.approval.approved),
      NilorNot(rip.approval.approved_by_id, aor.get.approval.approved_by_id),
      NilorNot(rip.approval.approved_at, aor.get.approval.approved_at)))

      .and(_.suspend setTo new Suspend(NilorNot(rip.suspend.suspended, aor.get.suspend.suspended),
        NilorNot(rip.suspend.suspended_at, aor.get.suspend.suspended_at),
        NilorNot(rip.suspend.suspended_till, aor.get.suspend.suspended_till)))

      .and(_.registration_ip_address setTo NilorNot(rip.registration_ip_address, aor.get.registration_ip_address))
      // .and(_.json_claz setTo NilorNot(rip.json_claz, aor.get.json_claz))

      .and(_.dates setTo new Dates(NilorNot(rip.dates.last_posted_at, aor.get.dates.last_posted_at),
        NilorNot(rip.dates.last_emailed_at, aor.get.dates.last_emailed_at),
        NilorNot(rip.dates.previous_visit_at, aor.get.dates.previous_visit_at),
          NilorNot(rip.dates.first_seen_at, aor.get.dates.first_seen_at),
            NilorNot(rip.dates.created_at, aor.get.dates.created_at)))
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

  //implicit val formats = DefaultFormats

  private def parseAccountInput(input: String): ValidationNel[Throwable, AccountInput] = {
    (Validation.fromTryCatchThrowable[AccountInput, Throwable] {
      parse(input).extract[AccountInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
  }

  private def generateAccountSet(id: String, m: AccountInput): ValidationNel[Throwable, AccountResult] = {
    (Validation.fromTryCatchThrowable[AccountResult, Throwable] {
    val dates = new Dates(m.dates.last_posted_at, m.dates.last_emailed_at, m.dates.previous_visit_at, m.dates.first_seen_at, Time.now.toString)
      AccountResult(id, m.name,  m.phone, m.email, m.api_key, m.password, m.states, m.approval,  m.suspend,  m.registration_ip_address,  dates)
    } leftMap { t: Throwable => new MalformedBodyError(m.json, t.getMessage) }).toValidationNel
  }

  private def orgChecker(email: String, orgs: Seq[OrganizationsResult], acc: AccountResult): ValidationNel[Throwable, AccountResult] = {
    val org_json = "{\"name\":\"" + DEFAULT_ORG_NAME + "\"}"
    val domain_json = "{\"name\":\"" + app.MConfig.domain + "\"}"
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
    if (update_account.password.password_reset_key == old_account.password.password_reset_key) {
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
