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

  //An Admin can delete an user
  //- delete billedhistories
  //- delete billingtransactions
  //- delete addons
  //- delete the snapshots
  //- delete disks
  //- delete components
  //- delete assembly
  //- delete assemblies
  //- delete organization
  //- delete accounts
  def delete(id: String): ValidationNel[Throwable, Option[AccountResult]] = {
    models.base.Accounts.update(id)
  }

  //Admin can suspend, impersonate, block, unblock, active users hack for 1.5.
  def update(input: String): ValidationNel[Throwable, Option[AccountResult]] = models.base.Accounts.update(input)

  def countAll: ValidationNel[Throwable, String] = models.base.Accounts.countAll

  def countAdmin: ValidationNel[Throwable, String] = list.flatMap { x =>
      val z = x.filter { y =>(y.states.authority!=null && y.states.authority.trim.length > 0) }
      Validation.success(z.map {_.states.authority.equalsIgnoreCase(ADMIN)}.filter(f => f).size.toString)
  }

}
