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
import models.tosca._


/**
 * @author rajthilak
 *
 */


object Availableunits extends Controller with APIAuthElement {
  
  /*
   * Create or update a new static units of megam marketplace items by email/json input. 
   */
  
  def post = StackAction(parse.tolerantText) {  implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("billing.Availableunits", "post:Entry"))
    
    (Validation.fromTryCatch[Result] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Request wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))
          play.api.Logger.debug(("%-20s -->[%s]").format("billing.Availableunits", "request funneled."))
          models.billing.Availableunits.create(email, clientAPIBody) match {
            case Success(succ) =>
              Status(CREATED)(
                FunnelResponse(CREATED, """Availableunits created successfully.
            |
            |You can use the the 'Availableunits':{%s}.""".format(succ.getOrElse("none")), "Megam::Availableunits").toJson(true))
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
