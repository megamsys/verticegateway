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
package controllers.camp

import scalaz._
import Scalaz._
import scalaz.NonEmptyList._

import scalaz.Validation._
import models.tosca._
import controllers.Constants._
import controllers.stack._
import controllers.stack.APIAuthElement
import controllers.funnel.FunnelResponse
import controllers.funnel.FunnelErrors._
import play.api._
import play.api.mvc._
import play.api.mvc.Result
import play.api.libs.iteratee._

/**
 * @author rajthilak
 *
 */

/*
 * 
 * If HMAC authentication is true then post or list the CSAR will be stored
 *  
 */
object CSARLinks extends Controller with APIAuthElement {

  /*
   * GET: findByName: Show a particular csar  by name 
   * Email provided in the URI.
   * Output: This is a special case, we need to return the yaml stored inside riak.
   * and in case of error, a json needs to be sent back.
   **/
  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("camp.CSARLinks", "show:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("name", id))

    (Validation.fromTryCatch[Result] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Request wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          play.api.Logger.debug(("%-20s -->[%s]").format("camp.CSARs", "request funneled."))

          models.tosca.CSARLinks.findByName(List(id).some) match {
            case Success(succ) =>
              Result(header = ResponseHeader(play.api.http.Status.OK, WithGzipHoleHeader),
                body = play.api.libs.iteratee.Enumerator((succ.head map (_.desc)).getOrElse("").getBytes))
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