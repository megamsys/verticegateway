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

import models.billing.BalancesResults
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import io.megam.auth.stack.Role.{ADMIN}

object Balances {

  //Admin can suspend, impersonate, block, unblock, active users hack for 1.5.
  def update(input: String): ValidationNel[Throwable, BalancesResults] =  {
      models.billing.Balances.update(input)
  }

}
