package models.admin

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import db._
import io.megam.auth.funnel.FunnelErrors._
import controllers.Constants._

import io.megam.auth.stack.AccountResult
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import io.megam.auth.stack.Role.{ADMIN}

object Users {

  //An Admin can list all the users
  def list: ValidationNel[Throwable, Seq[AccountResult]] = models.base.Accounts.list

  //Admin can suspend, impersonate, block, unblock, active users hack for 1.5.
  def update(input: String): ValidationNel[Throwable, Option[AccountResult]] =  {
      models.base.Accounts.update(input)
  }

  def countAll: ValidationNel[Throwable, String] = models.base.Accounts.countAll

  def countAdmin: ValidationNel[Throwable, String] = list.flatMap { x =>
      Validation.success(x.map {_.states.authority.equalsIgnoreCase(ADMIN)}.size.toString)
  }

}
