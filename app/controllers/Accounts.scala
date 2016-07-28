/*
** Copyright [2013-2016] [Megam Systems]
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
import scalaz.Validation
import scalaz.Validation.FlatMap._
import net.liftweb.json._

import io.megam.auth.funnel._
import io.megam.auth.funnel.FunnelErrors._
import play.api.mvc._
import org.apache.commons.codec.binary.Base64
import controllers.stack.Results
import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
/*
 * This controller performs onboarding a customer and registers an email/api_key
 * Output: FunnelResponse as JSON with the msg.
 */
object Accounts extends Controller with stack.APIAuthElement {

  /*
   * parse.tolerantText to parse the RawBody
   * get requested body and put into the scylla
   */
  def post = Action(parse.tolerantText) { implicit request =>
    val input = (request.body).toString()

    models.base.Accounts.create(input) match {
      case Success(succ) =>
        Status(CREATED)(
          FunnelResponse(CREATED, """Onboard successful. email '%s' and api_key '%s' is registered.""".
            format(succ.email, succ.api_key).stripMargin, "Megam::Account").toJson(true))
      case Failure(err) => {
        val rn: FunnelResponse = new HttpReturningError(err)
        Status(rn.code)(rn.toJson(true))
      }
    }
  }

  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>

  (Validation.fromTryCatchThrowable[Result, Throwable] {
    reqFunneled match {
      case Success(succ) => {
        val freq = succ.getOrElse(throw new Error("accounts wasn't funneled. Verify the header."))
        val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
    models.base.Accounts.findByEmail(id) match {
      case Success(succ) =>
      implicit val formats = DefaultFormats
          Ok(Results.resultset(models.Constants.ACCOUNTCLAZ, compactRender(Extraction.decompose(succ))))
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

  def reset(id: String) = Action(parse.tolerantText) { implicit request =>

    models.base.Accounts.reset(id) match {
      case Success(succ) =>
        Status(CREATED)(
          FunnelResponse(CREATED, """New password token generated successfully.
            |
            |You can use the 'Accounts':{%s}.""".format(succ.getOrElse("none")), "Megam::Account").toJson(true))
      case Failure(err) =>
        val rn: FunnelResponse = new HttpReturningError(err)
        Status(rn.code)(rn.toJson(true))
    }
  }

  def repassword = Action(parse.tolerantText) { implicit request =>
    val input = (request.body).toString()
    models.base.Accounts.repassword(input) match {
      case Success(succ) =>
        Status(CREATED)(
          FunnelResponse(CREATED, """Account reset successfully.
            |
            |You can use the 'Accounts':{%s}.""".format(succ.getOrElse("none")), "Megam::Account").toJson(true))
      case Failure(err) =>
        val rn: FunnelResponse = new HttpReturningError(err)
        Status(rn.code)(rn.toJson(true))
    }
  }

  def update = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Accounts wasn't funneled. Verify the header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          val clientAPIBody = freq.clientAPIBody.getOrElse(throw new Error("Body not found (or) invalid."))
          models.base.Accounts.update(email, clientAPIBody) match {
            case Success(succ) =>
              Status(CREATED)(
                FunnelResponse(CREATED, """Account updated successfully.
            |
            |You can use the 'Accounts':{%s}.""".format(succ.getOrElse("none")), "Megam::Account").toJson(true))
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
