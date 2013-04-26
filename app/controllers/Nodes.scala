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
import controllers.stack.HMACElement
import controllers.stack._
/**
 * @author ram
 *
 */
object Nodes extends Controller with HMACElement  {

  /*def Authenticated(f: (User, Request[AnyContent]) => Result) = {
  Action { request =>
    val result = for {      
       user <- Accounts.authenticate("bob@exam.com","secret")
    } yield f(user, request)
    result getOrElse Ok(views.html.index("Error Page"))
  }
}
  
def index = StackAction { implicit request =>
   Ok(views.html.index("Nodes Page"))
}*/

  def list = StackAction { implicit request =>
    Ok("Nodes Page succeeded")
  }
 
  //def list = SecurityActions.Authenticated { implicit request => 
   //         println("Validate entry")
   // Ok("Nodes Page succeeded")
  //}

  def show(id: Long) = StackAction { implicit request =>
    val title = "messages detail "
    Ok(views.html.index(title + id))
  }

  def post = StackAction { implicit request =>
    val title = "messages detail "
    Ok(views.html.index(title))
  }

}



