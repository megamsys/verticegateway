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

import scala.collection.immutable.Map
import scalaz._
import Scalaz._
import scalaz.NonEmptyList._

import scalaz.Validation._
import models._
import controllers.stack._
import controllers.stack.APIAuthElement
import controllers.funnel.{ FunnelResponse, FunnelResponses }
import controllers.funnel.FunnelErrors._
import controllers.Constants._
import org.megam.common.amqp._
import play.api._
import play.api.mvc._
import play.api.mvc.Result
/**
 * @author rajthilak
 *
 */
object MarketPlaceAddons extends Controller with APIAuthElement {

  /*
   * parse.tolerantText to parse the RawBody 
   * get requested body and put into the riak bucket
   */
  def post = StackAction(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.MarketPlaceAddons", "post:Entry"))

    (Validation.fromTryCatch[Result] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Request wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))
          play.api.Logger.debug(("%-20s -->[%s]").format("controllers.MarketPlaceAddons", "request funneled."))
          models.MarketPlaceAddons.create(email, clientAPIBody) match {
            case Success(succ) =>
              if (email.trim.equalsIgnoreCase(DEMO_EMAIL)) {
                Status(CREATED)(FunnelResponse(CREATED, """Marketplace Addon initiation dryrun submitted successfully.
            |
            |No actual addon in cloud. Signup for a new account to get started.""", "Megam::MarketPlaceAddons").toJson(true))
              } else {
                val tuple_succ = succ.getOrElse((Map.empty[String, String], "Bah", "hah"))
                var chainedComps = List[FunnelResponse]()
                var tohosts = List[String]()
                if (tuple_succ._3 != null) {
                  if (tuple_succ._3.contains(";")) {
                    tohosts = tuple_succ._3.split(";").toList
                  } else {
                    tohosts ::= tuple_succ._3
                  }
                }
                chainedComps = (tohosts map { //filter None, as foldRight creates it.
                  m =>
                    (CloudPerNodePublish(m, tuple_succ._1).dop.flatMap { x =>
                      play.api.Logger.debug(("%-20s -->[%s]").format("controllers.MarketPlaceAddons", "published successfully."))
                      FunnelResponse(CREATED, """MarketPlaceAddons disaster recovery tohosts created successfully.
            |
            |You can use the the 'MarketPlaceAddons host name':{%s}.""".format(m), "Megam::MarketPlaceAddons").successNel[Throwable]
                    } match {
                      //this is only a temporary hack.
                      case Success(succ_cpc) => succ_cpc
                      case Failure(err) =>
                        FunnelResponse(BAD_REQUEST, """MarketPlaceAddons initiation submission failed.
            |
            |Retry again, our queue servers are crowded""", "Megam::MarketPlaceAddons")
                    })
                })
                chainedComps ::= (CloudPerNodePublish(tuple_succ._2, tuple_succ._1).dop.flatMap { x =>
                  play.api.Logger.debug(("%-20s -->[%s]").format("controllers.MarketPlaceAddons", "published successfully."))
                  FunnelResponse(CREATED, """MarketPlaceAddons created successfully.
            |
            |You can use the the 'MarketPlaceAddons name':{%s}.""".format(succ.getOrElse("none")), "Megam::MarketPlaceAddons").successNel[Throwable]
                } match {
                  //this is only a temporary hack.
                  case Success(succ_cpc) => succ_cpc
                  case Failure(err) =>
                    FunnelResponse(BAD_REQUEST, """MarketPlaceAddons initiation submission failed.
            |
            |Retry again, our queue servers are crowded""", "Megam::MarketPlaceAddons")
                })
                Status(CREATED)(FunnelResponses.toJson(FunnelResponses(chainedComps), true))

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

  /*
   * GET: findByAppDefnsName: Show the MarketPlaceAddons for a  node name per user(by email)
   * Email grabbed from header
   * Output: JSON (MarketPlaceAddonsResults)  
   **/
  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.MarketPlaceAddons", "show:Entry"))

    (Validation.fromTryCatch[Result] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("MarketPlaceAddons wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          play.api.Logger.debug(("%-20s -->[%s]").format("controllers.MarketPlaceAddons", "request funneled."))

          models.MarketPlaceAddons.findByNodeName(List(id).some) match {
            case Success(succ) =>
              Ok(MarketPlaceAddonsResults.toJson(succ, true))
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