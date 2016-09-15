package controllers

import scalaz._
import Scalaz._
import scalaz.Validation._
import net.liftweb.json._

import io.megam.auth.funnel._
import io.megam.auth.funnel.FunnelErrors._
import play.api.mvc._
import controllers.stack.Results

object Snapshots extends Controller with controllers.stack.APIAuthElement {

implicit val formats = DefaultFormats

def post = StackAction(parse.tolerantText) { implicit request =>
  (Validation.fromTryCatchThrowable[Result, Throwable] {
    reqFunneled match {
      case Success(succ) => {
        val freq = succ.getOrElse(throw new Error("Invalid header."))
        val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
        val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))
        models.snapshots.Snapshots.create(email, clientAPIBody) match {
          case Success(succ) =>
            Status(CREATED)(
              FunnelResponse(CREATED, "Snapshot created successfully.", "Megam::Snapshots").toJson(true))
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

  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Invalid header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          models.snapshots.Snapshots.findById(id,email) match {
            case Success(succ) => Ok(Results.resultset(models.Constants.SNAPSHOTSCOLLECTIONCLAZ, compactRender(Extraction.decompose(succ))))
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

  def list = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Invalid header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          models.snapshots.Snapshots.findByEmail(email) match {
            case Success(succ) => Ok(Results.resultset(models.Constants.SNAPSHOTSCOLLECTIONCLAZ, compactRender(Extraction.decompose(succ))))
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
