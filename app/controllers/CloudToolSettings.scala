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

import scalaz._
import Scalaz._
import scalaz.NonEmptyList._

import scalaz.Validation._
import play.api._
import play.api.mvc._
import play.api.mvc.SimpleResult
import models._
import controllers.stack._
import controllers.stack.APIAuthElement
import controllers.funnel.FunnelResponse
import controllers.funnel.FunnelErrors._
import org.megam.common.amqp._

/**
 * @author rajthilak
 *
 */

/*
 * 
 * If HMAC authentication is true then post or list the CloudToolSettings are executed
 *  
 */
object CloudToolSettings extends Controller with APIAuthElement {

  /*
   * Create or update a new CloudToolSetting by email/json input. 
   * Old value for the same key gets wiped out.
   */
  def post = StackAction(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.CloudToolSettings", "post:Entry"))

    (Validation.fromTryCatch[SimpleResult] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Request wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))
          play.api.Logger.debug(("%-20s -->[%s]").format("controllers.CloudToolSetting", "request funneled."))
          models.CloudToolSettings.create(email, clientAPIBody) match {
            case Success(succ) =>
              val tuple_succ = succ.getOrElse(("Nah", "Bah"))
              CloudToolPublish(tuple_succ._2).dop.flatMap { x =>
                play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Requests", "published successfully."))
                Status(CREATED)(
                  FunnelResponse(CREATED, """CloudToolSettings created successfully.
            |
            |You can use the the 'CloudToolSetting name':{%s}.""".format(succ.getOrElse("none")), "Megam::CloudToolSetting").toJson(true)).successNel[Throwable]   
              } match {
                //this is only a temporary hack.
                case Success(succ_cpc) => succ_cpc
                case Failure(err) =>
                  Status(BAD_REQUEST)(FunnelResponse(BAD_REQUEST, """CloudToolSettings submission failed.
            |
            |Retry again, our queue servers are crowded""", "Megam::CloudToolSetting").toJson(true))
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
    }).fold(succ = { a: SimpleResult => a }, fail = { t: Throwable => Status(BAD_REQUEST)(t.getMessage) })

  }

  /*
   * GET: findByName: Show a particular CloudToolSetting by name 
   * Email provided in the URI.
   * Output: JSON (CloudToolSettingResult)
   **/
  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.CloudToolSettings", "show:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("name", id))

    (Validation.fromTryCatch[SimpleResult] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Request wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          play.api.Logger.debug(("%-20s -->[%s]").format("controllers.CloudToolSettings", "request funneled."))

          models.CloudToolSettings.findByName(List(id).some) match {
            case Success(succ) =>
              Ok(CloudToolSettingResults.toJson(succ, true))
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

  /**
   * GET: findbyEmail: List all the predef cloud names per email
   * Email grabbed from header.
   * Output: JSON (CloudToolSettingResult)
   */
  def list = StackAction(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.CloudToolSettings", "list:Entry"))

    (Validation.fromTryCatch[SimpleResult] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Request wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          play.api.Logger.debug(("%-20s -->[%s]").format("controllers.CloudToolSettings", "request funneled."))
          models.CloudToolSettings.findByEmail(email) match {
            case Success(succ) =>
              Ok(CloudToolSettingResults.toJson(succ, true))
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