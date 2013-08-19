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
import controllers.funnel.{ FunnelResponse, FunnelResponses }
import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }
import models._
import play.api._
import play.api.mvc._
import scalaz._
import scalaz.Validation._

/**
 * @author rajthilak
 *
 */
object Application extends Controller with APIAuthElement {

  /**
   * Make this role based (Admin),
   * based on a private key.
   */
  def init = Action { implicit request =>
    PlatformAppPrimer.prep match {
      case Success(succ) => {
        val sFlash = ("success" -> "Megam platform is primed. Go!")
        Redirect("/").flashing(sFlash, ("success1" -> FunnelResponses.toJson(succ,true)))       
      }
      case Failure(err) => {
        val rn: FunnelResponses = new HttpReturningError(err)
        val rnjson = FunnelResponses.toJson(rn, false)
        Logger.debug(rnjson)
        Redirect("/").flashing(
          "error0" -> "Duh Megam platform isn't primed Yet.. NoGo!",
          "error1" -> err.toString)
      }
    }

  }

  def index = Action { implicit request =>
    Ok(views.html.index("Megam play at your service. Lets kick the tyres."))
  }
  /**
   * POST : Authenticate, verifies if the auth setup is OK.
   * Output: FunnelResponse as JSON with the msg.
   */
  def authenticate = StackAction(parse.tolerantText) { implicit request =>
    val resp = FunnelResponse(apiAccessed.getOrElse("Something strange. Authentication successful, but sans success message. Contact support"), "Megam::Auth").toJson(true)
    Logger.debug(resp)
    Ok(resp)
  }

}