package models

import play.api.db._
import play.api.Play.current

case class Authenticate(email: String, password: String)

object Authenticate {  
  
  
  def authenticate(email: String, password: String): Option[Authenticate] = {
    Option(Authenticate("admin", "admin"))
  }
  
}