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
import scalaz.Validation._

import play.api._
import play.api.mvc._
import models._
import controllers.funnel.FunnelErrors._
import controllers.stack.APIAuthElement
import controllers.stack._
import org.megam.common.amqp._
import scalaz.Validation._
import play.api.mvc.Result

/**
 * @author ram
 *
 */

/*
 * this controller for HMAC authentication and access riak
 * If HMAC authentication is true then post or list the nodes are executed
 *  
 */
object Nodes extends Controller with APIAuthElement {

  /*
   * parse.tolerantText to parse the RawBody 
   * get requested body and put into the riak bucket
   */
  def post = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatch[Result] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Request wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))
          models.Nodes.create(email, clientAPIBody) match {
            case Success(succ) =>
              Ok("""Node initiation instruction submitted successfully.
            |
            |Check back on the 'node name':{%s}
            |The cloud is working for you. It will be ready shortly.""".format(succ.getOrElse("none")))
            case Failure(err) =>
              val rn = new HttpReturningError(err)
              Status(rn.code.getOrElse(NOT_IMPLEMENTED))(rn.getMessage)
          }
        }
        case Failure(err) => {
          val rn = new HttpReturningError(err)
          Status(rn.code.getOrElse(NOT_IMPLEMENTED))(rn.getMessage)
        }
      }
    }).fold(succ = { a: Result => a }, fail = { t: Throwable => Status(BAD_REQUEST)(t.getMessage) })
  }

  /*
   * List the nodes for a particular email. The email is parsed from the header using
   * funneling (implicit). 
   * A model riak call findByNodeName, return the node details of that node.
   * 
   */
  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatch[Result] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Request wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          models.Nodes.findByNodeName(email) match {
            case Success(succ) =>
              Ok("""%s""".format(succ.getOrElse("none")))
            case Failure(err) =>
              val rn = new HttpReturningError(err)
              Status(rn.code.getOrElse(NOT_IMPLEMENTED))(rn.getMessage)
          }
        }
        case Failure(err) => {
          val rn = new HttpReturningError(err)
          Status(rn.code.getOrElse(NOT_IMPLEMENTED))(rn.getMessage)
        }
      }
    }).fold(succ = { a: Result => a }, fail = { t: Throwable => Status(BAD_REQUEST)(t.getMessage) })

  }

  /*
   * List the nodes for a particular email. The email is parsed from the header using
   * funneling (implicit). 
   * A model riak call findByEmail, return the node details of that node.
   * 
   */
  def list = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatch[Result] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Request wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          models.Nodes.findByEmail(email) match {
            case Success(succ) =>
              Ok("""%s""".format(succ.getOrElse("none")))
            case Failure(err) =>
              val rn = new HttpReturningError(err)
              Status(rn.code.getOrElse(NOT_IMPLEMENTED))(rn.getMessage)
          }
        }
        case Failure(err) => {
          val rn = new HttpReturningError(err)
          Status(rn.code.getOrElse(NOT_IMPLEMENTED))(rn.getMessage)
        }
      }
    }).fold(succ = { a: Result => a }, fail = { t: Throwable => Status(BAD_REQUEST)(t.getMessage) })
  }
}