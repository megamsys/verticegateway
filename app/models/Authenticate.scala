package models

import play.api.db._
import play.api.Play.current
import controllers.stack.APIAuthElement
import controllers.stack._
import controllers.funnel.FunnelErrors._
import controllers.funnel.FunnelResponse
import models._
import play.api._
import play.api.mvc._
import play.api.mvc.Result
import scalaz._
import scalaz.Validation._
import Scalaz._

case class UserData(email: String, apikey: String)

object Authenticate {

  def authenticate(email: String, apikey: String): Option[UserData] = {
    models.Accounts.findByEmail(email) match {
      case Success(succ) => {
        play.api.Logger.debug(("%-20s -->[%s]").format("Account Result", succ.get.api_key))
        val userEmail = succ.get.email
        email match {
          case userEmail if apikey == succ.get.api_key =>           
            Some(UserData(succ.get.email, succ.get.api_key))
          case userEmail if apikey != succ.get.api_key =>            
            None
          case _ =>            
            None
        }
      }
      case Failure(err) => {
        None
      }
    }   
  }

}