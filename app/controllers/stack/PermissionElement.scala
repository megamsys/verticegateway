package controllers.stack

import scala.concurrent.{ ExecutionContext, Future }
import jp.t2v.lab.play2.stackc.{RequestWithAttributes, StackableController}
import scala.concurrent.Future
import scalaz._
import Scalaz._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import net.liftweb.json._
import net.liftweb.json.JsonParser._
import play.api.mvc.{Result, Request, Controller}
import play.api.data._

trait PermissionElement extends StackableController {
    self: Controller =>

  private val authBagOpt = "token".some

  /*private def canPermit(request: Request[_]): Boolean = (for {
    (user.role, authority) match {
      case (Administrator, _)       => true
      case (RegularUser, RegularUser) => true
      case _                        => false
    }
    tokenInForm    <- authBagOpt
  } yield tokenInForm == tokenInSession).getOrElse(false) */

  private def canPermit(request: Request[_]): Boolean =  true

  override def proceed[A](request: RequestWithAttributes[A])(f: RequestWithAttributes[A] => Future[Result]): Future[Result] = {
    if (canPermit(request)) super.proceed(request)(f)
    else Future.successful(BadRequest)
  }

}
