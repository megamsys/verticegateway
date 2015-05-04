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

object Assembly extends Controller with APIAuthElement {

  /*
   * GET: findByNodeName: Show requests for a  node name per user(by email)
   * Email grabbed from header
   * Output: JSON (AssembliesResults)
   **/
  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Assembly", "show:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("nodename", id))

    (Validation.fromTryCatch[Result] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Assembly wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Assembly", "request funneled."))

          models.tosca.Assembly.findByNodeName(List(id).some) match {
            case Success(succ) =>
              Ok(AssemblyResults.toJson(succ, true))
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

  def update = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatch[Result] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Assemblies wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))
          models.tosca.Assembly.update(email, clientAPIBody) match {
            case Success(succ) =>
              //Ok(AssemblyResults.toJson(succ, true))
              val tuple_succ = succ.getOrElse((Map.empty[String, String], "Bah"))
              CloudPerNodePublish(tuple_succ._2, tuple_succ._1).dop.flatMap { x =>
                play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Assembly", "published successfully."))
                Status(CREATED)(FunnelResponse(CREATED, """CatUpdate initiation instruction submitted successfully.
            |
            |The App update request is working for you. It will be ready shortly.""", "Megam::Assembly").toJson(true)).successNel[Throwable]
              } match {
                //this is only a temporary hack.
                case Success(succ_cpc) => succ_cpc
                case Failure(err) =>
                  Status(BAD_REQUEST)(FunnelResponse(BAD_REQUEST, """AppUpdateRequest initiation submission failed.
            |
            |Retry again, our queue servers are crowded""", "Megam::Assembly").toJson(true))
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
    }).fold(succ = { a: Result => a }, fail = { t: Throwable => Status(BAD_REQUEST)(t.getMessage) })
  }



 def build(id: String, cid: String) = Action(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Assembly", "show:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("Assembly ID", id))
    play.api.Logger.debug(("%-20s -->[%s]").format("Component ID", cid))

    (Validation.fromTryCatch[Result] {
         ContiniousIntegrationNotifyPublish(id, cid, "notify").dop.flatMap { x =>
                play.api.Logger.debug(("%-20s -->[%s]").format("controllers.CINotify", "published successfully."))
                Status(CREATED)(FunnelResponse(CREATED, """CI notify initiation instruction submitted successfully.
            |
            |The Assembly build request submitted. It will be build ready shortly.""", "Megam::Assembly").toJson(true)).successNel[Throwable]
              } match {
                //this is only a temporary hack.
                case Success(succ_cpc) => succ_cpc
                case Failure(err) =>
                  Status(BAD_REQUEST)(FunnelResponse(BAD_REQUEST, """CI notification failed.
            |
            |Retry again, our queue servers are crowded""", "Megam::Assembly").toJson(true))
              }
    }).fold(succ = { a: Result => a }, fail = { t: Throwable => Status(BAD_REQUEST)(t.getMessage) })
 }


}
