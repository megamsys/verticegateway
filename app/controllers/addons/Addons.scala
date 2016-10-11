package controllers.addons

import scalaz._
import Scalaz._
import scalaz.NonEmptyList._
import scalaz.Validation._

import io.megam.auth.funnel._
import io.megam.auth.funnel.FunnelErrors._
import models.billing._
import play.api.mvc._
import controllers.stack.Results
import net.liftweb.json._
import net.liftweb.json.JsonParser._
/**
 * @author ranjitha
 *
 */


object Addons extends Controller with controllers.stack.APIAuthElement {
  implicit val formats = DefaultFormats
  /**
   * Create a new Addons for the user.
   **/

  def post = StackAction(parse.tolerantText) {  implicit request =>
    (Validation.fromTryCatchThrowable[Result,Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Invalid header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))
          models.addons.Addons.create(email, clientAPIBody) match {
            case Success(succ) =>
              Status(CREATED)(
                FunnelResponse(CREATED, """Addons created successfully.
            |
            |You can use the the 'Addons':{%s}.""".format(succ.getOrElse("none")), "Megam::Addons").toJson(true))
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

   def show(name: String) = StackAction(parse.tolerantText) { implicit request =>
     (Validation.fromTryCatchThrowable[Result, Throwable] {
       reqFunneled match {
         case Success(succ) => {
           val freq = succ.getOrElse(throw new Error("Invalid header."))
           val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
           models.addons.Addons.findById(email,name) match {
             case Success(succ) => Ok(Results.resultset(models.Constants.ADDONSCOLLECTIONCLAZ, compactRender(Extraction.decompose(succ))))
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
