package controllers

import scalaz._
import Scalaz._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import net.liftweb.json._

import io.megam.auth.funnel._
import io.megam.auth.funnel.FunnelErrors._
import play.api.mvc._
import controllers.stack.Results
import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }


object Accounts extends Controller with stack.APIAuthElement {

  
  def login = Action(parse.tolerantText) { implicit request =>
    val input = (request.body).toString()

    models.base.Accounts.login(input) match {
      case Success(succ) =>
         implicit val formats = DefaultFormats
          Status(FOUND)(Results.resultset(models.Constants.ACCOUNTCLAZ, compactRender(Extraction.decompose(succ))))
      case Failure(err) => {
        val rn: FunnelResponse = new HttpReturningError(err)
        Status(rn.code)(rn.toJson(true))
      }
    }
  }
  
  def post = Action(parse.tolerantText) { implicit request =>
    val input = (request.body).toString()

    models.base.Accounts.create(input) match {
      case Success(succ) =>
        Status(CREATED)(
          FunnelResponse(CREATED, """Onboard successful. email '%s' and api_key '%s' is registered.""".
            format(succ.email, "●●●●●●●●●").stripMargin, "Megam::Account").toJson(true))
      case Failure(err) => {
        val rn: FunnelResponse = new HttpReturningError(err)
        Status(rn.code)(rn.toJson(true))
      }
    }
  }

  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Invalid header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
      models.base.Accounts.findByEmail(id) match {
        case Success(succ) =>
            implicit val formats = DefaultFormats
            Ok(Results.resultset(models.Constants.ACCOUNTCLAZ, compactRender(Extraction.decompose(succ))))
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

  def forgot(id: String) = Action(parse.tolerantText) { implicit request =>
    models.base.Accounts.forgot(id) match {
      case Success(succ) =>
        Status(CREATED)(
          FunnelResponse(CREATED, "Password token generated successfully.", "Megam::Account").toJson(true))
      case Failure(err) =>
        val rn: FunnelResponse = new HttpReturningError(err)
        Status(rn.code)(rn.toJson(true))
    }
  }

  def password_reset = Action(parse.tolerantText) { implicit request =>
    val input = (request.body).toString()
    models.base.Accounts.password_reset(input) match {
      case Success(succ) =>
        Status(CREATED)(
          FunnelResponse(CREATED, "Password reset successfully.", "Megam::Account").toJson(true))
      case Failure(err) =>
        val rn: FunnelResponse = new HttpReturningError(err)
        Status(rn.code)(rn.toJson(true))
    }
  }

  def update = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Invalid header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))
          models.base.Accounts.update(email, clientAPIBody) match {
            case Success(succ) =>
              Status(CREATED)(
                FunnelResponse(CREATED, "Your profile updated successfully.", "Megam::Account").toJson(true))
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
