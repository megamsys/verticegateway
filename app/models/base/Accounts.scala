package models.base

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import cache._
import db._
import io.megam.auth.funnel.FunnelErrors._
import controllers.Constants._

import io.megam.auth.stack.AccountResult
import io.megam.auth.stack.{ Name, Phone, Password, States, Approval, Dates, Suspend }
import io.megam.auth.stack.SecurePasswordHashing
import io.megam.auth.stack.SecurityActions

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
 *
 */
case class AccountInput(name: Name, phone: Phone, email: String, api_key: String, password: Password, states: States, approval: Approval, suspend: Suspend, registration_ip_address: String,  dates: Dates) {
  val json = "{\"name\":" +name.json+",\"phone\":" + phone.json + ",\"email\":\"" + email + "\",\"api_key\":\"" + api_key + "\",\"password\":" + password.json + ",\"states\":" + states.json + ",\"approval\":" + approval.json + ",\"suspend\":" + suspend.json + ",\"registration_ip_address\":\"" + registration_ip_address + "\",\"dates\":" + dates.json + "}"
}

case class AccountReset(password_reset_key: String, password_reset_sent_at: String) {
  val password = Password(password_reset_key, password_reset_sent_at)
  val json = "{\"id\":\""+ "\",\"name\":" + Name.empty.json+",\"phone\":" + Phone.empty.json + ",\"email\":\"" + "\",\"api_key\":\""  + "\",\"password\":" + password.json + ",\"states\":" + States.empty.json + ",\"approval\":" + Approval.empty.json + ",\"suspend\":" + Suspend.empty.json + ",\"registration_ip_address\":\"" + "\",\"dates\":" + Dates.empty.json + "}"

}

sealed class AccountSacks extends CassandraTable[AccountSacks, AccountResult] {
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

  override lazy val tableName = "accounts"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def dbInsert(account: AccountResult): ValidationNel[Throwable, ResultSet] = {
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
      .value(_.dates, account.dates)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def dbGet(email: String): ValidationNel[Throwable, Option[AccountResult]] = {
    val res = select.where(_.email eqs email).one()
    Await.result(res, 5.seconds).successNel
  }

  def dbSelectAll(email: String, org: String): ValidationNel[Throwable, Seq[AccountResult]] = {
    val res = select.fetch
    Await.result(res, 5.seconds).successNel
  }


  def dbUpdate(email: String, rip: AccountResult, aor: Option[AccountResult]): ValidationNel[Throwable, ResultSet] = {
    val res = update.where(_.email eqs NilorNot(email, aor.get.email))
      .modify(_.id setTo NilorNot(rip.id, aor.get.id))
      .and(_.name setTo new Name(NilorNot(rip.name.first_name, aor.get.name.first_name),
         NilorNot(rip.name.last_name, aor.get.name.last_name)))


       .and(_.phone setTo new Phone(NilorNot(rip.phone.phone, aor.get.phone.phone),
       NilorNot(rip.phone.phone_verified, aor.get.phone.phone_verified)))

      .and(_.api_key setTo NilorNot(rip.api_key, aor.get.api_key))

      .and(_.password setTo new Password(NilorNot(rip.password.password_hash, aor.get.password.password_hash),
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

      .and(_.dates setTo new Dates(NilorNot(rip.dates.last_posted_at, aor.get.dates.last_posted_at),
        NilorNot(rip.dates.last_emailed_at, aor.get.dates.last_emailed_at),
        NilorNot(rip.dates.previous_visit_at, aor.get.dates.previous_visit_at),
          NilorNot(rip.dates.first_seen_at, aor.get.dates.first_seen_at),
            NilorNot(rip.dates.created_at, aor.get.dates.created_at)))
      .future()
    Await.result(res, 5.seconds).successNel
  }

  private def NilorNot(rip: String, aor: String): String = {
    rip == null || rip == "" match {
      case true => return aor
      case false => return rip
    }
  }

}
object Accounts extends ConcreteAccounts {

  ///////////////// All these conversion stuff should move out. ///////////
  // 1. Get me an account input object from a string
  // 2. Get me an account result object from account_input
  // 3. Get me a clone of account result with password hashed
  // 3. Get me a account result with passticket verified and mutated with new password hash
  // 5. Get me a account result with passticket updated.
  private def parseAccount(input: String): ValidationNel[Throwable, AccountInput] = {
    (Validation.fromTryCatchThrowable[AccountInput, Throwable] {
      parse(input).extract[AccountInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
  }

  private def mkAccountResult(id: String, m: AccountInput): ValidationNel[Throwable, AccountResult] = {
    (Validation.fromTryCatchThrowable[AccountResult, Throwable] {
      val dates = new Dates(m.dates.last_posted_at, m.dates.last_emailed_at, m.dates.previous_visit_at,
          m.dates.first_seen_at, Time.now.toString)

      val pwd = new Password(SecurePasswordHashing.hashPassword(m.password.password_hash),"","")
      AccountResult(id, m.name,  m.phone, m.email, m.api_key, pwd, m.states, m.approval,  m.suspend,  m.registration_ip_address,  dates)
    } leftMap { t: Throwable => new MalformedBodyError(m.json, t.getMessage) }).toValidationNel
  }

  private def mkAccountResultDup(m: AccountResult): ValidationNel[Throwable, AccountResult] = {
    (Validation.fromTryCatchThrowable[AccountResult, Throwable] {
      if(m.password!=null && m.password.password_hash!=null && m.password.password_hash.trim.length >0) {
        val pwd = new Password(SecurePasswordHashing.hashPassword(m.password.password_hash),"","")

        AccountResult(m.id, m.name,  m.phone, m.email, m.api_key, pwd, m.states, m.approval,  m.suspend,  m.registration_ip_address,  m.dates)
      } else {
        AccountResult(m.id, m.name,  m.phone, m.email, m.api_key, m.password, m.states, m.approval,  m.suspend,  m.registration_ip_address,  m.dates)
      }
    }).toValidationNel
  }

  private def mkAccountResultWithPassword(m: AccountResult, old: AccountResult): ValidationNel[Throwable, AccountResult] = {
    if (m.password.password_reset_key == old.password.password_reset_key) {
      val pwd =  new Password(SecurePasswordHashing.hashPassword(m.password.password_hash),"","")

      val mupd = AccountResult(m.id, m.name,  m.phone, m.email, m.api_key, pwd, m.states, m.approval,  m.suspend,  m.registration_ip_address,  m.dates)

      Validation.success[Throwable, AccountResult](mupd).toValidationNel
    } else {
      Validation.failure[Throwable, AccountResult](new CannotAuthenticateError(m.email, "Password token didn't match.")).toValidationNel
    }
  }

  private def mkAccountResultWithToken(t: String): ValidationNel[Throwable, AccountResult] = {
      val pwd =  new Password("",t, Time.now.toString)
      val m = AccountResult("dum")

      val mupd = AccountResult("", m.name,  m.phone, "", m.api_key, pwd, m.states, m.approval,  m.suspend,  m.registration_ip_address,  m.dates)

      Validation.success[Throwable, AccountResult](mupd).toValidationNel

  }
  ///////////////// All these conversion stuff should move out. ///////////

  private def mkOrgIfEmpty(email: String, orgs: Seq[OrganizationsResult], acc: AccountResult): ValidationNel[Throwable, AccountResult] = {
    val org_json    = "{\"name\":\"" + app.MConfig.org + "\"}"
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


  def login(input: String): ValidationNel[Throwable, AccountResult] = {
    for {
      p <- parseAccount(input)
      a <- (Accounts.findByEmail(p.email) leftMap { t: NonEmptyList[Throwable] => t })
      s <- SecurityActions.Validate(p.password.password_hash, a.get.password.password_hash)
      e <- Events(a.get.id, EVENTUSER, Events.LOGIN, Map(EVTEMAIL -> p.email)).createAndPub()
    } yield {
     a.get
    }
  }

  def create(input: String): ValidationNel[Throwable, AccountResult] = {
    for {
      p   <- parseAccount(input)
      uir <- (UID("act").get leftMap { err: NonEmptyList[Throwable] => err })
      ast <- mkAccountResult(uir.get._1 + uir.get._2, p)
      ins <- dbInsert(ast)
      org <- Organizations.findByEmail(p.email)
      res <- mkOrgIfEmpty(p.email, org, ast)
      evn <- Events(ast.id, EVENTUSER, Events.ONBOARD, Map(EVTEMAIL -> ast.email)).createAndPub()
    } yield {
      res
    }
  }


  def update(email: String, input: String): ValidationNel[Throwable, Option[AccountResult]] = {
    val accountResult: ValidationNel[Throwable, AccountResult] = (Validation.fromTryCatchThrowable[AccountResult, Throwable] {
      parse(input).extract[AccountResult]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      t <- accountResult
      c <- mkAccountResultDup(t)
      a <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      d <- dbUpdate(email, c, a)
    } yield {
      a
    }
  }

  def forgot(email: String): ValidationNel[Throwable, Option[AccountResult]] = {
    val token = generateToken(26)

    for {
      a <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      s <- mkAccountResultWithToken(token)
      d <- dbUpdate(email,s, a)
      e <- Events(a.get.id, EVENTUSER, Events.RESET, Map(EVTEMAIL -> a.get.email, EVTTOKEN -> token)).createAndPub()
    } yield {
      a
    }
  }


  def password_reset(input: String): ValidationNel[Throwable, Option[AccountResult]] = {
    val accountResult: ValidationNel[Throwable, AccountResult] = (Validation.fromTryCatchThrowable[AccountResult, Throwable] {
      parse(input).extract[AccountResult]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      c  <- accountResult
      a  <- (Accounts.findByEmail(c.email) leftMap { t: NonEmptyList[Throwable] => t })
      m  <- (mkAccountResultWithPassword(c, a.get) leftMap { t: NonEmptyList[Throwable] => t })
      u  <- (dbUpdate(c.email, m, a) leftMap { t: NonEmptyList[Throwable] => t})
    } yield {
      c.some
    }
  }


    def findByEmail(email: String): ValidationNel[Throwable, Option[AccountResult]] = {
    InMemory[ValidationNel[Throwable, Option[AccountResult]]]({
      name: String =>
        {
          play.api.Logger.debug(("%-20s -->[%s]").format("LIV", email))
          (dbGet(email) leftMap { t: NonEmptyList[Throwable] =>
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

  //Only Admin authority can list users hack for 1.5.
  def listAll(email: String, org: String): ValidationNel[Throwable, Seq[AccountResult]] = {
    (dbSelectAll(email, org) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(email, "Users = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[AccountResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[AccountResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[AccountResult]](new ResourceItemNotFound(email, "Users = nothing found.")).toValidationNel
    }
  }

  implicit val sedimentAccountEmail = new Sedimenter[ValidationNel[Throwable, Option[AccountResult]]] {
    def sediment(maybeASediment: ValidationNel[Throwable, Option[AccountResult]]): Boolean = {
       maybeASediment.isSuccess
    }
  }

  private def generateToken(length: Int): String = {
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    generateTokenFrom(length, chars)
  }

  private def generateTokenFrom(length: Int, chars: Seq[Char]): String = {
    val sb = new StringBuilder
    for (i <- 1 to length) {
      val randomNum = util.Random.nextInt(chars.length)
      sb.append(chars(randomNum))
    }
    sb.toString
  }

}
