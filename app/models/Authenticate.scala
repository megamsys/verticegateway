package models

import play.api.db._
import play.api.Play.current

case class Authenticate(email: String, apikey: String)

object Authenticate {  
  
  
  def authenticate(email: String, apikey: String): Option[Authenticate] = {
    
    Option(Authenticate("admin", "admin"))
  }
  
}