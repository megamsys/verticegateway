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

import play.api._
import play.api.mvc._
import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }
import models._
import controllers.stack._

/**
 * @author rajthilak
 *
 */
object Application extends Controller with AuthElement {

  def index = Action {
    Ok(views.html.index("Your new application is Ready."))
  }

  def authenticate = StackAction(parse.tolerantText) { implicit request =>
    Ok("Your Authentication success")
  }

  /* def authenticate = Action { implicit request =>
    println("start act")
    Ok(views.html.index("Authentication page"))
    /**
     *  Here we need to plugin the http://soa.dzone.com/articles/protect-rest-service-using-0
     *
     */
    val guser: GuestUser = null /** ideally load it **/
    val maybeUser: Option[User] = null
    val user: User = maybeUser.getOrElse(guser)
    gotoLoginSucceeded(user.id)
    
  }

  /**
   * Return the `gotoLogoutSucceeded` method's result in the staleauth action.
   *
   * Since the `gotoLogoutSucceeded` returns `Result`,
   * If you import `jp.t2v.lab.play2.auth._`, you can add a procedure like the following.
   *
   *   gotoLogoutSucceeded.flashing(
   *     "success" -> "You've been logged out"
   *   )
   */
  def staleauth = Action { implicit request =>
     //do something...
    gotoLogoutSucceeded
  }*/

}
