package controllers.billing

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
import models.billing._
import controllers.stack.Results
import controllers.stack.{APIAuthElement, PermissionElement}

/**
 * @author rajesh
 *
 */

object Billingtransactions extends Controller with APIAuthElement with PermissionElement {
implicit val formats = DefaultFormats
  /**
   * Create a new billing transcations for the user.
   **/

  def post = StackAction(parse.tolerantText, AuthorityKey -> Administrator) {  implicit request =>
    (Validation.fromTryCatchThrowable[Result,Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq   = succ.getOrElse(throw new CannotAuthenticateError("Invalid header.", "Read docs.megam.io/api."))
          val email  = freq.maybeEmail.getOrElse(throw new CannotAuthenticateError("Email not found (or) invalid.", "Read docs.megam.io/api."))
          val org    = freq.maybeOrg.getOrElse(throw new CannotAuthenticateError("Org not found (or) invalid.", "Read docs.megam.io/api."))
          val admin  = canPermit(grabAuthBag).getOrElse(throw new PermissionNotThere("admin authority is required to access this resource.", "Read docs.megam.io/api."))
          val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))

          models.billing.Billingtransactions.create(email, clientAPIBody) match {
            case Success(succ) =>
              Status(CREATED)(
                FunnelResponse(CREATED, "Billingtransaction created."  , "Megam::BillingTransactions").toJson(true))
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

 /**
   * GET: findbyEmail: List all the billing transcations per email
   * Email grabbed from header.   * Output: JSON (BillingtransactionsResult)
   */

  def list = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatchThrowable[Result,Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Invalid header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          models.billing.Billingtransactions.findByEmail(email) match {
            case Success(succ) =>
              Ok(Results.resultset(models.Constants.BILLINGTRANSCATIONSCOLLECTIONCLAZ, compactRender(Extraction.decompose(succ))))
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
