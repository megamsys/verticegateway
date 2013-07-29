/* 
** Copyright [2012-2013] [Megam Systems]
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

import play.api._
import play.api.mvc._
import play.api.mvc.Result

import scalaz._
import scalaz.Validation._

import models._
import controllers.stack.APIAuthElement
import controllers.stack._
import controllers.funnel.FunnelErrors._
import controllers.funnel.FunnelResponse
/**
 * @author rajthilak
 *
 */

/*
 * This controller performs onboarding a customer and registers an email/api_key 
 * into riak.
 * Output: FunnelResponse as JSON with the msg.  
 */
object Accounts extends Controller with APIAuthElement {

  /*
   * parse.tolerantText to parse the RawBody 
   * get requested body and put into the riak bucket
   */
  def post = Action(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Accounts", "post:Entry"))
    val input = (request.body).toString()
    play.api.Logger.debug(("%-20s -->[%s]").format("input", input))
    models.Accounts.create(input) match {
      case Success(succ) => Status(CREATED)(
        FunnelResponse(CREATED,"""Account created successfully.
            |
            |Your email '%s' and api_key '%s' registered successully.  Hurray ! Run the other API calls now.""".
          format(succ.get.email, succ.get.api_key).stripMargin,"Megam::Account").toJson(true))
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
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Accounts", "show:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", id))
    models.Accounts.findByEmail(id) match {
      case Success(succ) => {
        Ok((succ.map(s => s.toJson(true))).getOrElse(
          AccountResult(id).toJson(true)))
      }
      case Failure(err) => {
        val rn: FunnelResponse = new HttpReturningError(err)
        Status(rn.code)(rn.toJson(true))
      }
    }

  }
}