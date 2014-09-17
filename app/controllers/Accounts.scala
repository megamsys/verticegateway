/* 
** Copyright [2013-2014] [Megam Systems]
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

import controllers.stack.APIAuthElement
import controllers.stack._
import controllers.funnel.FunnelErrors._
import controllers.funnel.FunnelResponse
import models._
import play.api._
import play.api.mvc._
import play.api.mvc.Result
import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

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
      case Success(succ) =>
        PlatformAppPrimer.clone_predefcloud(succ.get.email).flatMap { x =>
          Status(CREATED)(
            FunnelResponse(CREATED, """Onboard successful.
            |
            |email '%s' and api_key '%s' is registered - @megam.""".
              format(succ.get.email, succ.get.api_key).stripMargin, "Megam::Account").toJson(true)).successNel[Error]
        } match {
          case Success(succ_cpc) => succ_cpc
          case Failure(errcpc) =>
            val rncpc: FunnelResponse = new HttpReturningError(errcpc)
            Status(rncpc.code)(rncpc.toJson(true))
        }
        PlatformAppPrimer.clone_cloudtoolsettings(succ.get.email).flatMap { x =>
          Status(CREATED)(
            FunnelResponse(CREATED, """Onboard successful.
            |
            |email '%s' and api_key '%s' is registered - @megam.""".
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