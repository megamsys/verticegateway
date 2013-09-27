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

import controllers.funnel.FunnelErrors._
import controllers.funnel.FunnelResponse
import controllers.stack.APIAuthElement
import controllers.stack._
import models._
import play.api._
import play.api.mvc._
import play.api.mvc.SimpleResult
import scalaz._
import Scalaz._
import scalaz.Validation._

/**
 * @author rajthilak
 *
 */

/*
 * this controller for HMAC authentication and access riak
 * If HMAC authentication is true then post or list the predefs are executed
 *  
 */
object CloudTools extends Controller with APIAuthElement {

  /**
   * GET: findbyName: List a cloud_depname by name
   * Output: JSON (CloudDeployerResult)
   * This is global and has no tie to email or node.
   */
  def show(cloud_toolname: String) = StackAction(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.CloudTools", "show:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("name", cloud_toolname))

    models.CloudTools.findByName(Stream(cloud_toolname).some) match {
      case Success(succ) => {
        Ok(CloudToolResults.toJson(succ, true)) //implicit transformer doesn't work.
      }
      case Failure(err) => {
        val rn: FunnelResponse = new HttpReturningError(err)
        Status(rn.code)(rn.toJson(true))
      }
    }

  }

  /**
   * GET: listKeys: List all the predefs as available now in the predefs bucket.
   * Output: JSON (List[PredefResult])
   */
  def list = StackAction(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.CloudTools", "list:Entry"))

    (Validation.fromTryCatch[SimpleResult] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Request wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          models.CloudTools.listAll match {
            case Success(succ) => {
              Ok(CloudToolResults.toJson(succ, true));
            }
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
    }).fold(succ = { a: SimpleResult => a }, fail = { t: Throwable => Status(BAD_REQUEST)(t.getMessage) })

  }

}