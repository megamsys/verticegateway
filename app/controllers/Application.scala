package controllers

import play.api._
import play.api.mvc._
//import jp.t2v.lab.play2.auth._
import jp.t2v.lab.play2.auth.Auth
import app.controllers.stack._
import jp.t2v.lab.play2.stackc.{RequestWithAttributes, RequestAttributeKey, StackableController}
import models._

object Application extends Controller with HMACAccessElement  with Auth with AuthConfigImpl {

 
  
  def index = Action {
   Ok(views.html.index("Your new application is Ready."))
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
