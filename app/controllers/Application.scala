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

import controllers.stack._
import controllers.funnel.FunnelErrors._
import controllers.funnel._
import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }
import models._
import play.api._
import play.api.mvc._
import scalaz._
import Scalaz._
import scalaz.Validation._
import play.api.data._
import play.api.data.Form._
import play.api.data.Forms._
/**
 * @author rajthilak
 *
 */

object Application extends Controller with APIAuthElement {
  
  //Shows welcome page.
  def index = Action { implicit request =>
    Ok(views.html.index("Megam CMP. Lets kick the tyres."))
  }
  
   /**
   * POST : Authenticate, verifies if the auth setup is OK.
   * Output: FunnelResponse as JSON with the msg.
   * Accessed via API
   */
  def authenticate = StackAction(parse.tolerantText) { implicit request =>
    val resp = FunnelResponse(apiAccessed.getOrElse("Something strange. Authentication successful, but sans success message. Contact support"), "Megam::Auth").toJson(true)
    Ok(resp)
  }
 
  //Called from /utru, and redirects to 
  def initaccount = Action { implicit request =>
    PlatformAppPrimer.acc_prep match {
      case Success(succ) => {
        val fu = List(("success" -> "Megam CMP : sandbox account is ready.")) ++ FunnelResponses.toTuple2(succ)
        Redirect("/").flashing(fu: _*) //a hack to covert List[Tuple2] to varargs of Tuple2. flashing needs it.
      }
      case Failure(err) => {
        val rn: FunnelResponses = new HttpReturningError(err)
        val rnjson = FunnelResponses.toJson(rn, false)
        val fu = List(("error" -> "Duh Megam CMP couldn't be primed.")) ++ FunnelResponses.toTuple2(rn)
        Redirect("/").flashing(fu: _*)
      }
    }
  }

 /**
   * Login page.
   */
  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }
  
  val loginForm = Form(
    mapping(
      "email" -> text,
      "apikey" -> text)(UserData.apply)(UserData.unapply) verifying ("Invalid email or apikey", result => result match {
        case userdata => Authenticate.authenticate(userdata.email, userdata.apikey).isDefined
      }))

  def validate(email: String, apikey: String) = {
    email match {
      case "a@b.com" if apikey == "admin" =>
        Some(UserData(email, apikey))
      case "a@b.com" if apikey != "admin" =>
        None
      case _ =>
        None
    }
  }
  
 

  /**
   * Handle login form submission.
   */
  def loginauthenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      user => Redirect("/init"))
      //user => Ok(views.html.initialize(loginForm, "Megam CMP. Lets kick the tyres.")))
  }  
  
  /**
   * Make this role based (Admin),
   * based on a private key.
   */
  def init = Action { implicit request =>
    PlatformAppPrimer.prep match {
      case Success(succ) => {
        val fu = List(("success" -> "Megam CMP is ready.")) ++ FunnelResponses.toTuple2(succ)
        Redirect("/").flashing(fu: _*) //a hack to covert List[Tuple2] to varargs of Tuple2. flashing needs it.
      }
      case Failure(err) => {
        val rn: FunnelResponses = new HttpReturningError(err)
        val rnjson = FunnelResponses.toJson(rn, false)
        val fu = List(("error" -> "Duh Megam CMP couldn't be primed.")) ++ FunnelResponses.toTuple2(rn)
        Logger.debug(rnjson)
        Redirect("/").flashing(fu: _*)
      }
    }

  }
  
   def initcloudtoolsetting = Action { implicit request =>
    PlatformAppPrimer.cts_prep match {
      case Success(succ) => {
        val fu = List(("success" -> "Megam CMP :Cloud provisioner is ready.")) ++ FunnelResponses.toTuple2(succ)
        Redirect("/").flashing(fu: _*) //a hack to covert List[Tuple2] to varargs of Tuple2. flashing needs it.
      }
      case Failure(err) => {
        val rn: FunnelResponses = new HttpReturningError(err)
        val rnjson = FunnelResponses.toJson(rn, false)
        val fu = List(("error" -> "Duh Megam CMP :Cloud provisioner couldn't be primed.")) ++ FunnelResponses.toTuple2(rn)
        Redirect("/").flashing(fu: _*)
      }
    }
  }

  def initmarketplaceaddons = Action { implicit request =>
    PlatformAppPrimer.mkp_prep match {
      case Success(succ) => {
        val fu = List(("success" -> "Megam CMP :Default Market Place Addons is ready.")) ++ FunnelResponses.toTuple2(succ)
        Redirect("/").flashing(fu: _*) //a hack to covert List[Tuple2] to varargs of Tuple2. flashing needs it.
      }
      case Failure(err) => {
        val rn: FunnelResponses = new HttpReturningError(err)
        val rnjson = FunnelResponses.toJson(rn, false)
        val fu = List(("error" -> "Duh Megam CMP :Default Market Place Addons couldn't be primed.")) ++ FunnelResponses.toTuple2(rn)
        Redirect("/").flashing(fu: _*)
      }
    }
  }
  
}