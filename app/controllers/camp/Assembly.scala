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
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
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
   * GET: findById: Show assembly information from assembly_id per user(by email)
   * Email grabbed from header
   * Output: JSON (AssemblyResults)
   **/
  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Assembly", "show:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("id", id))

    (Validation.fromTryCatchThrowable[Result,Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Assembly wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Assembly", "request funneled."))

          models.tosca.Assembly.findById(List(id).some) match {
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
    (Validation.fromTryCatchThrowable[Result,Throwable] {
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



  def build(id: String, name: String) = Action(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Assembly", "buildCI:Entry"))
    val DOCKERQUEUE = "dockerstate"

  (Validation.fromTryCatchThrowable[Result,Throwable] {
          val clientAPIBody = "{\"cat_id\": \"" + id + "\",\"cattype\":\"" + "" + "\",\"name\":\"" + name + "\",\"action\":\"" + "redeploy" + "\"}"

          models.Requests.createforExistNode(clientAPIBody) match {
            case Success(succ) =>              
                val tuple_succ = succ.getOrElse((Map.empty[String, String], "Bah", "nah", "hah", "lah"))
                var qName = ""
                   if (tuple_succ._3 != "Microservices") {
                          qName = tuple_succ._2
                    } else {
                        qName = DOCKERQUEUE
                    }         
                CloudPerNodePublish(qName, tuple_succ._1).dop.flatMap { x =>
                  Status(CREATED)(FunnelResponse(CREATED, """CatRequest initiation instruction submitted successfully.
            |
            |The CatRequest is processed by our engine. It will be ready shortly.""", "Megam::CatRequests").toJson(true)).successNel[Throwable]
                } match {
                  //this is only a temporary hack.
                  case Success(succ_cpc) => succ_cpc
                  case Failure(err) =>
                    Status(BAD_REQUEST)(FunnelResponse(BAD_REQUEST, """CatRequest initiation submission failed.
            |
            |Retry again, our queue servers are crowded""", "Megam::Requests").toJson(true))
                }
            case Failure(err) => {
              val rn: FunnelResponse = new HttpReturningError(err)
              Status(rn.code)(rn.toJson(true))
            }
          }
    
    }).fold(succ = { a: Result => a }, fail = { t: Throwable => Status(BAD_REQUEST)(t.getMessage) })
  }
  
  
}
