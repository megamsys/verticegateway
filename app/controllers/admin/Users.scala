package controllers.admin

import scalaz._
import Scalaz._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import net.liftweb.json._
import net.liftweb.json.JsonParser._

import io.megam.auth.funnel.{ FunnelResponse, FunnelResponses }
import io.megam.auth.funnel.FunnelErrors._
import play.api.mvc._
import controllers.stack.Results
import controllers.stack.{APIAuthElement, PermissionElement}
import controllers.stack.Role._

object Users extends Controller with APIAuthElement with PermissionElement {
  implicit val formats = DefaultFormats

  def list = StackAction(parse.tolerantText, AuthorityKey -> Administrator) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Invalid header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val org = freq.maybeOrg.getOrElse(throw new Error("Org not found (or) invalid."))
          models.tosca.Assemblies.findByEmail(email, org) match {
            case Success(succ) => Ok(Results.resultset(models.Constants.ADMINUSERSCOLLECTIONCLAZ, compactRender(Extraction.decompose(succ))))
            case Failure(err) =>
              val rn: FunnelResponse = new HttpReturningError(err)
              Status(rn.code)(rn.toJson(true))
          }
        }
        case Failure(err) => {
          val rn: FunnelResponse = new HttpReturningError(err)
          Status(rn.code)(rn.toJson(true))
        }
      }
    }).fold(succ = { a: Result => a }, fail = { t: Throwable => Status(BAD_REQUEST)(t.getMessage) })
  }

}
