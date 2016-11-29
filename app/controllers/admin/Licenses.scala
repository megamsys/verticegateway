package controllers.admin

import scalaz._
import Scalaz._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import net.liftweb.json._
import net.liftweb.json.JsonParser._

import io.megam.auth.funnel.{ FunnelResponse, FunnelResponses }
import io.megam.auth.funnel.FunnelErrors._
import io.megam.auth.stack.Role._

import play.api.mvc._
import controllers.stack.Results
import controllers.stack.{APIAuthElement, PermissionElement}

object Licenses extends Controller with APIAuthElement with PermissionElement {

  def post = StackAction(parse.tolerantText, AuthorityKey -> Administrator) { implicit request =>
    val input = (request.body).toString()

    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq   = succ.getOrElse(throw new CannotAuthenticateError("Invalid header.", "Read docs.megam.io/api."))
          val email  = freq.maybeEmail.getOrElse(throw new CannotAuthenticateError("Email not found (or) invalid.", "Read docs.megam.io/api."))
          val org    = freq.maybeOrg.getOrElse(throw new CannotAuthenticateError("Org not found (or) invalid.", "Read docs.megam.io/api."))
          val admin  = canPermit(grabAuthBag).getOrElse(throw new PermissionNotThere("admin authority is required to access this resource.", "Read docs.megam.io/api."))

          models.admin.Licenses.create(input) match {
            case Success(succ) => {
              Status(CREATED)(
                FunnelResponse(CREATED, """License saved '%s'.""".
                  format("●●●●●●●●●").stripMargin, "Megam::License").toJson(true))
           }
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
    }).fold(succ = { a: Result => a }, fail = { t: Throwable =>   { val rn: FunnelResponse = new HttpReturningError(nels(t));  Status(rn.code)(rn.toJson(true)) } })
  }

  def show(id: String) = StackAction(parse.tolerantText, AuthorityKey -> Administrator) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq   = succ.getOrElse(throw new CannotAuthenticateError("Invalid header.", "Read docs.megam.io/api."))
          val email  = freq.maybeEmail.getOrElse(throw new CannotAuthenticateError("Email not found (or) invalid.", "Read docs.megam.io/api."))
          val org    = freq.maybeOrg.getOrElse(throw new CannotAuthenticateError("Org not found (or) invalid.", "Read docs.megam.io/api."))
          val admin  = canPermit(grabAuthBag).getOrElse(throw new PermissionNotThere("admin authority is required to access this resource.", "Read docs.megam.io/api."))

          models.admin.Licenses.findById(id) match {
            case Success(succ) => {
              implicit val formats = DefaultFormats

              Ok(Results.resultset(models.Constants.ADMINLICENSECLAZ, compactRender(Extraction.decompose(succ))))
             }
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
    }).fold(succ = { a: Result => a }, fail = { t: Throwable =>   { val rn: FunnelResponse = new HttpReturningError(nels(t));  Status(rn.code)(rn.toJson(true)) } })
  }

  def reports(starttime: String, endtime: String) = StackAction(parse.tolerantText, AuthorityKey -> Administrator) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq   = succ.getOrElse(throw new CannotAuthenticateError("Invalid header.", "Read docs.megam.io/api."))
          val email  = freq.maybeEmail.getOrElse(throw new CannotAuthenticateError("Email not found (or) invalid.", "Read docs.megam.io/api."))
          val org    = freq.maybeOrg.getOrElse(throw new CannotAuthenticateError("Org not found (or) invalid.", "Read docs.megam.io/api."))
          val admin  = canPermit(grabAuthBag).getOrElse(throw new PermissionNotThere("admin authority is required to access this resource.", "Read docs.megam.io/api."))


          models.admin.Licenses.listReports(starttime, endtime) match {
            case Success(succ) => {
              implicit val formats = DefaultFormats

              Ok(Results.resultset(models.Constants.ADMINLICENSECLAZ, compactRender(Extraction.decompose(succ))))
             }
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
    }).fold(succ = { a: Result => a }, fail = { t: Throwable =>   { val rn: FunnelResponse = new HttpReturningError(nels(t));  Status(rn.code)(rn.toJson(true)) } })
  }

}
