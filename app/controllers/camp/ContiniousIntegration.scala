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
package controllers.camp

import scalaz._
import Scalaz._
import scalaz.NonEmptyList._

import scalaz.Validation._
import models._
import models.tosca._
import controllers.Constants.DEMO_EMAIL
import controllers.stack._
import controllers.stack.APIAuthElement
import controllers.funnel.{ FunnelResponse, FunnelResponses }
import controllers.funnel.FunnelErrors._
import org.megam.common.amqp._
import play.api._
import play.api.mvc._
import play.api.mvc.Result

object ContiniousIntegration extends Controller with APIAuthElement {

  /*
   * parse.tolerantText to parse the RawBody
   * get requested body and put into the riak bucket
   */
  def post = StackAction(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.ContiniousIntegration", "post:Entry"))

    (Validation.fromTryCatchThrowable[Result,Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Application ContiniousIntegration wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))
          play.api.Logger.debug(("%-20s -->[%s]").format("controllers.ContiniousIntegration", "request funneled."))
          models.tosca.ContiniousIntegration.create(email, clientAPIBody) match {
            case Success(succ) =>
                /*This isn't correct. Revisit, as the testing progresses.
               We need to trap success/failures.
               */
              /*  val tuple_succ = succ.getOrElse("Nah", "Bah")
                CloudPerNodePublish(tuple_succ._2, tuple_succ._1).dop.flatMap { x =>
                  play.api.Logger.debug(("%-20s -->[%s]").format("controllers.ContiniousIntegration", "published successfully."))
                  Status(CREATED)(FunnelResponse(CREATED, """ContiniousIntegration initiation instruction submitted successfully.
            |
            |The ContiniousIntegration is working for you. It will be ready shortly.""", "Megam::ContiniousIntegration").toJson(true)).successNel[Throwable]
                } match {
                  //this is only a temporary hack.
                  case Success(succ_cpc) => succ_cpc
                  case Failure(err) =>
                    Status(BAD_REQUEST)(FunnelResponse(BAD_REQUEST, """ContiniousIntegration initiation submission failed.
            |
            |Retry again, our queue servers are crowded""", "Megam::ContiniousIntegration").toJson(true))
                } */

              Status(CREATED)(FunnelResponse(CREATED, """ContiniousIntegration initiation instruction submitted successfully.
            |
            |The ContiniousIntegration is working for you. It will be ready shortly.""", "Megam::ContiniousIntegration").toJson(true))
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



}
