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

import io.megam.auth.stack.Role
import io.megam.auth.stack.Role._
import io.megam.auth.stack._

trait PermissionElement {
    self: Controller =>

  def canPermit(authOpt: Option[AuthBag]): Option[Boolean] =    {
    play.api.Logger.debug(("%-20s -->[%s]").format(" ----- canpermit sstarts ", authOpt))
    if ((authOpt.map { auth: AuthBag =>
      (Role.valueOf(auth.authority), Role.valueOf("admin")) match {
        case (Administrator,_)             => true
        case (RegularUser, RegularUser)    => false
        case _                             => false
        }
    }).get) true.some else none
  }

}
