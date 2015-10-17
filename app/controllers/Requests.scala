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
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import models._
import controllers.stack._
import controllers.Constants.{ DEMO_EMAIL, DELETE_REQUEST , CREATE_REQUEST}
import controllers.stack.APIAuthElement
import controllers.funnel.FunnelResponse
import controllers.funnel.FunnelErrors._
import org.megam.common.amqp._
import play.api._
import play.api.mvc._
import play.api.mvc.Result
/**
 * @author ram
 *
 */
object Requests extends Controller with APIAuthElement {



  /*
   * parse.tolerantText to parse the RawBody
   * get requested body and put into the riak bucket
   */
  def post = StackAction(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Requests", "post:Entry"))

    (Validation.fromTryCatchThrowable[Result,Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Request wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))
          play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Request", "request funneled."))
          models.Requests.createforExistNode(clientAPIBody) match {
            case Success(succ) =>
              val tuple_succ = succ.getOrElse((Map.empty[String, String], "Bah", "nah", "hah", "lah"))
              //This isn't correct. Revisit, as the testing progresses.We need to trap success/failures.
              if (email.trim.equalsIgnoreCase(DEMO_EMAIL))
                Status(CREATED)(FunnelResponse(CREATED, """Request initiation dryrun submitted successfully.
            |
            |Dry launch of {:node_name=>'%s', :req_type=>'%s'}
            |No actual launch in cloud. Signup for a new account to get started.""".format(tuple_succ._2, tuple_succ._3).stripMargin, "Megam::Request").toJson(true))
              else {
                val pubres = if (tuple_succ._3 != "Microservices") {
                  if (tuple_succ._4.trim.equalsIgnoreCase(DELETE_REQUEST)) {
                    for {
                    csup <- CloudStandUpPublish(tuple_succ._2, tuple_succ._1).dop
                   } yield {}

                   } else {
                    for {
                     csup <- CloudPerNodePublish(tuple_succ._2, tuple_succ._1).dop
                   } yield {}

                  }
                } else {
                    for {
                     csup <- CloudPerNodePublish(MConfig.dockerup_queue, tuple_succ._1).dop
                   } yield {}
                  }

               play.api.Logger.debug(("%-20s -->[%s]").format("controllers.node.update", pubres))
                pubres flatMap { x =>
                  play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Requests", "published successfully."))
                  Status(CREATED)(FunnelResponse(CREATED, """Request initiation instruction submitted successfully.
            |
            |Check on the node for further updates. It will be ready shortly.""", "Megam::Request").toJson(true)).successNel[Throwable]
                } match {
                  //this is only a temporary hack.
                  case Success(succ_cpc) => succ_cpc
                  case Failure(err) =>
                    Status(BAD_REQUEST)(FunnelResponse(BAD_REQUEST, """Request initiation submission failed.
            |
            |Retry again, our queue servers are crowded""", "Megam::Request").toJson(true))
                }
              }
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
