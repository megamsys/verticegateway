/* 
** Copyright [2013-2015] [Megam Systems]
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
package models

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
