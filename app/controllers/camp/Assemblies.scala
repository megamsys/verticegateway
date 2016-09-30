package controllers.camp

import scalaz._
import Scalaz._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import net.liftweb.json._
import net.liftweb.json.JsonParser._

import io.megam.auth.funnel.{ FunnelResponse, FunnelResponses }
import io.megam.auth.funnel.FunnelErrors._
import models.tosca._
import play.api.mvc._
import controllers.stack.Results

object Assemblies extends Controller with controllers.stack.APIAuthElement {
  implicit val formats = DefaultFormats

  def post = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Invalid header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))
          new models.tosca.Launcher(apiAccessed).launch(clientAPIBody) match {
            case Success(wrapasm) =>
              Status(CREATED)(
                FunnelResponse(CREATED, """[%s,%s] deployment submitted successfully.""".format(
                  wrapasm.map(_.get.assemblies.mkString("|")).mkString,wrapasm.map(_.get.id).mkString), "Megam::Assemblies").toJson(true)
            )
          
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

  /*
   * GET: findById: Show requests for a  node name per user(by email)
   * Email grabbed from header
   * Output: JSON (AssembliesResults)
   **/
  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Invalid header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          models.tosca.Assemblies.findById(List(id).some) match {
            case Success(succ) =>
              Ok(Results.resultset(models.Constants.ASSEMBLIESCOLLECTIONCLAZ, compactRender(Extraction.decompose(succ))))
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

  /*
   * GET: findbyEmail: List all the Assemblies per email
   * Email grabbed from header.
   * Output: JSON (AssembliesResult)
   */
  def list = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Invalid header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val org = freq.maybeOrg.getOrElse(throw new Error("Org not found (or) invalid."))
          models.tosca.Assemblies.findByEmail(email, org) match {
            case Success(succ) => Ok(Results.resultset(models.Constants.ASSEMBLIESCOLLECTIONCLAZ, compactRender(Extraction.decompose(succ))))
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
