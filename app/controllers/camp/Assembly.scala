package controllers.camp

import scalaz._
import Scalaz._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import net.liftweb.json._
import controllers.stack.Results

import models.tosca._
import io.megam.auth.funnel.{ FunnelResponse, FunnelResponses }
import io.megam.auth.funnel.FunnelErrors._
import play.api.mvc._

object Assembly extends Controller with controllers.stack.APIAuthElement {
  implicit val formats = DefaultFormats
  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Invalid header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          models.tosca.Assembly.findById(List(id).some) match {
            case Success(succ) =>
              Ok(Results.resultset(models.Constants.ASSEMBLYCOLLECTIONCLAZ, compactRender(Extraction.decompose(succ))))
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

  def update = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Assemblies wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))
          val org = freq.maybeOrg.getOrElse(throw new Error("Org not found (or) invalid."))
          models.tosca.Assembly.update(org, clientAPIBody) match {
            case Success(succ) => {
              Status(CREATED)(FunnelResponse(CREATED, """Bind initiation submitted successfully.
            |
            |Engine is cranking.""", "Megam::Assembly").toJson(true))
            }
            case Failure(err) => {
              val rn: FunnelResponse = new HttpReturningError(err)
              Status(rn.code)(rn.toJson(true))
            }
          }
        }
        case Failure(err) => {
          val rn: FunnelResponse = new HttpReturningError(err)
          Status(rn.code)(rn.toJson(true))
        }
      }
    }).fold(succ = { a: Result => a }, fail = { t: Throwable => Status(BAD_REQUEST)(t.getMessage) })
  }

  //publicly exposed API. Tighten it later.
  def upgrade(id: String) = Action(parse.tolerantText) { implicit request =>
    models.tosca.Assembly.upgrade("", id) match {
      case Success(succ) => {
        Status(CREATED)(FunnelResponse(CREATED, """Your upgrade is in process.
            |
            |Engine is cranking.""", "Megam::Assembly").toJson(true))
      }
      case Failure(err) => {
        val rn: FunnelResponse = new HttpReturningError(err)
        Status(rn.code)(rn.toJson(true))
      }
    }
  }
}
