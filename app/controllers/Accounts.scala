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
package controllers

import scalaz._
import Scalaz._
import scalaz.Validation
import scalaz.Validation.FlatMap._


import controllers.funnel.FunnelResponse
import controllers.funnel.FunnelErrors._
import play.api.mvc._
/*
 * This controller performs onboarding a customer and registers an email/api_key
 * into riak
 * Output: FunnelResponse as JSON with the msg.
 */
object Accounts extends Controller with stack.APIAuthElement {

  /*
   * parse.tolerantText to parse the RawBody
   * get requested body and put into the riak bucket
   */
  def post = Action(parse.tolerantText) { implicit request =>
    val input = (request.body).toString()
    models.base.Accounts.create(input) match {
      case Success(succ) =>
        utils.PlatformAppPrimer.clone_organizations(succ.get.email).flatMap { x =>
          Status(CREATED)(
            FunnelResponse(CREATED, """Onboard successful. email '%s' and api_key '%s' is registered.""".
              format(succ.get.email, succ.get.api_key).stripMargin, "Megam::Account").toJson(true)).successNel[Error]
        } match {
          case Success(succ_cpc) => succ_cpc
          case Failure(errcpc) =>
            val rncpc: FunnelResponse = new HttpReturningError(errcpc)
            Status(rncpc.code)(rncpc.toJson(true))
        }

      case Failure(err) => {
        val rn: FunnelResponse = new HttpReturningError(err)
        Status(rn.code)(rn.toJson(true))
      }
    }
  }

  /*
   * GET: findByEmail: Show a particular account by email
   * Email provided in the URI.
   * Output: JSON (AccountsResult)
   **/
  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    models.base.Accounts.findByEmail(id) match {
      case Success(succ) => {
        Ok((succ.map(s => s.toJson(true))).getOrElse(
          models.base.AccountResult(id).toJson(true)))
      }
      case Failure(err) => {
        val rn: FunnelResponse = new HttpReturningError(err)
        Status(rn.code)(rn.toJson(true))
      }
    }

  }

  def update = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatchThrowable[Result,Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Accounts wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))
          models.base.Accounts.updateAccount(email, clientAPIBody) match {
           case Success(succ) =>
              Status(CREATED)(
                FunnelResponse(CREATED, """Account updated successfully.
            |
            |You can use the the 'Accounts':{%s}.""".format(succ.getOrElse("none")), "Megam::Account").toJson(true))
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
