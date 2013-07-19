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

import scalaz._
import Scalaz._
import play.api._
import play.api.mvc._
import models._
import controllers.funnel.FunnelErrors._
import controllers.funnel.FunnelResponse

import controllers.stack.APIAuthElement
import controllers.stack._
import org.megam.common.amqp._
import scalaz.Validation._
import play.api.mvc.Result

/**
 * @author rajthilak
 *
 */

/*
 * this controller for HMAC authentication and access riak
 * If HMAC authentication is true then post or list the predefs are executed
 *  
 */
object Predefs extends Controller with APIAuthElement {

  /**
   * GET: findbyName: List a predef name by name
   * Output: JSON (PredefsResult)
   * This is global and has no tie to email or node.
   */
  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Predefs", "show:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("name", id))

    models.Predefs.findByName(id) match {
      case Success(succ) => {
        Ok((succ.map(s => s.toString)).getOrElse(
          """No Predef exists in your predef's list '%s'. Locate returned null.
            |
            |Read https://api.megam.co, http://docs.megam.co to know about our API.Ask for help on the forums.""".
            format(id, tailMsg)))
      }
      case Failure(err) => {
        val rn: FunnelResponse = new HttpReturningError(err)
        Status(rn.code)(rn.toJson(true))
      }
    }

  }

  /**
   * GET: listKeys: List all the predefs as available now in the predefs bucket.
   * Output: JSON (List[PredefsResult])
   */
  def list = StackAction(parse.tolerantText) { implicit request =>
    play.api.Logger.debug(("%-20s -->[%s]").format("controllers.Predefs", "list:Entry"))

    models.Predefs.listKeys match {
      case Success(succ) => {
        Ok(succ.mkString("\n"))
      }
      case Failure(err) => {
        val rn: FunnelResponse = new HttpReturningError(err)
        Status(rn.code)(rn.toJson(true))
      }
    }
  }

}