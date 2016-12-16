package controllers.stack

import scalaz._
import Scalaz._
import scalaz.Validation._
import scala.concurrent.Future

import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }

import controllers.Constants._
import io.megam.auth.funnel._
import io.megam.auth.funnel.FunnelErrors._
import io.megam.auth.stack.AccountResult
import io.megam.auth.stack.MasterKeyResult
import play.api.mvc._
import play.api.libs.iteratee.Enumerator
import models.base.Accounts
import models.base.MasterKeys
import io.megam.auth.stack.Role._
import io.megam.auth.stack.{RequestAttributeKeyConstants}

/**
 * @author rajthilak
 *
 */
/*
 * sub trait for stackable controller, proceed method was override here for our request changes,
 * And result return in super trait proceed method,
 * when stack action is called then this stackable controller is executed
 */
trait APIAuthElement extends io.megam.auth.stack.AuthElement with RequestAttributeKeyConstants with ImplicitJsonFormats {
  self: Controller =>

  override def authImpl(input: String): ValidationNel[Throwable, Option[AccountResult]] = {
    Accounts.findByEmail(input)
  }

  override def masterImpl(input: String): ValidationNel[Throwable, Option[MasterKeyResult]] = {
    MasterKeys.findById(input)
  }

}
