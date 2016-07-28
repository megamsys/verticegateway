package controllers

import scalaz._
import Scalaz._
import scalaz.Validation._
import net.liftweb.json._

import io.megam.auth.funnel._
import io.megam.auth.funnel.FunnelErrors._
import play.api.mvc._
import controllers.stack.Results

object EventsBilling extends Controller with controllers.stack.APIAuthElement {
  def show(limit: String) = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          implicit val formats = DefaultFormats
          val freq = succ.getOrElse(throw new Error("Events wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))
          models.events.EventsBilling.findById(email, clientAPIBody, limit) match {
            case Success(succ) => Ok(Results.resultset(models.Constants.EVENTSBILLINGCOLLECTIONCLAZ, compactRender(Extraction.decompose(succ))))
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

  def list(limit: String) = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          implicit val formats = DefaultFormats
          val freq = succ.getOrElse(throw new Error("Events wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          models.events.EventsBilling.findByEmail(email, limit) match {
            case Success(succ) => Ok(Results.resultset(models.Constants.EVENTSBILLINGCOLLECTIONCLAZ, compactRender(Extraction.decompose(succ))))
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

  def index = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          implicit val formats = DefaultFormats
          val freq = succ.getOrElse(throw new Error("Events wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          models.events.EventsBilling.IndexEmail(email) match {
            case Success(succ) => Ok(Results.resultset(models.Constants.EVENTSBILLINGCOLLECTIONCLAZ, compactRender(Extraction.decompose(succ))))

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
