/*
** Copyright [2013-2015] [Megam Systems]
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

package controllers.billing

import scalaz._
import Scalaz._
import scalaz.NonEmptyList._

import scalaz.Validation._
import models._
import controllers.stack._
import controllers.stack.APIAuthElement
import controllers.funnel.FunnelResponse
import controllers.funnel.FunnelErrors._
import org.megam.common.amqp._
import play.api._
import play.api.mvc._
import play.api.mvc.Result
import models.billing._


/**
 * @author rajthilak
 *
 */


object Invoices extends Controller with APIAuthElement {

  /**
   * Create a new billing history for the user.
   **/

  def post = StackAction(parse.tolerantText) {  implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("billing.Invoices", "post:Entry"))

    (Validation.fromTryCatchThrowable[Result,Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Request wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))
          play.api.Logger.debug(("%-20s -->[%s]").format("billing.Invoices", "request funneled."))
          models.billing.Invoices.create(email, clientAPIBody) match {
            case Success(succ) =>
              Status(CREATED)(
                FunnelResponse(CREATED, """Invoices created successfully.
            |
            |You can use the the 'Invoices':{%s}.""".format(succ.getOrElse("none")), "Megam::Invoices").toJson(true))
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

 /**
   * GET: findbyEmail: List all the billing histories per email
   * Email grabbed from header.
   * Output: JSON (BillingHistoriesResult)
   */
  def list = StackAction(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Invoices", "list:Entry"))

    (Validation.fromTryCatchThrowable[Result,Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Request wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Invoices", "request funneled."))
          models.billing.Invoices.findByEmail(email) match {
            case Success(succ) =>
              Ok(InvoicesResults.toJson(succ, true))
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
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Invoices", "show:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("name", id))

    (Validation.fromTryCatchThrowable[Result,Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Request wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Invoices", "request funneled."))

          models.billing.Invoices.findByName(List(id).some) match {
            case Success(succ) =>
              Ok(InvoicesResults.toJson(succ, true))
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
