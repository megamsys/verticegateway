package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Form._
import play.api.data.Forms._
import views._
import scalaz._
import Scalaz._
import models.Authenticate

case class UserData(email: String, password: String)

object Authentication extends Controller {

  val loginForm = Form(
    mapping(
      "email" -> text,
      "password" -> text
    )(UserData.apply)(UserData.unapply)  verifying ("Invalid email or password", result => result match {
        case userdata => Authenticate.authenticate(userdata.email, userdata.password).isDefined
    })
  )

  /**
* Login page.
*/
  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  /**
* Logout and clean the session.
*/
  def logout = Action {
    Redirect("/").withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }

  /**
* Handle login form submission.
*/
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => Redirect("/").withSession("email" -> user.email)
    )
  }

}