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
import models._
import controllers.stack.APIAuthElement
import controllers.stack._
import scalaz._
import scalaz.Validation._
import play.api.mvc.Result

/**
 * @author rajthilak
 *
 */

/*
 * This controller performs onboarding a customer and registers an email/api_key 
 * into riak.
 *   
 */
object Accounts extends Controller with APIAuthElement {

  /*
   * parse.tolerantText to parse the RawBody 
   * get requested body and put into the riak bucket
   */
  def post = Action(parse.tolerantText) { implicit request =>
    val input = (request.body).toString()
    Logger.debug("Accounts.post  : entry\n" + input)
    models.Accounts.create(input) match {
      case Success(succ) => Ok("""Account created successfully.
            |
            |Your email '%s' and api_key '%s' registered successully.  Hurray ! Run the other API calls now. 
            |Read https://api.megam.co, http://docs.megam.co to know about our API.Ask for help on the forums.""".
        format(succ.get.email, succ.get.api_key))
      case Failure(err) => InternalServerError(err.map(l => l.getMessage).list.mkString("\n"))
    }
  }

  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    models.Accounts.findByEmail(id) match {
      case Success(succ) => {
        Ok((succ.map(s => s.toString)).getOrElse(
          """No Account exists for email '%s'. Locate returned null.
            |
            |Please check if email '%s' with an api_key isn't registered to consume api.megam.co.    
            |Read https://api.megam.co, http://docs.megam.co to know about our API.Ask for help on the forums.""".
            format(id)))
      }
      case Failure(err) => {
        InternalServerError("""Account for email '%s' doesn't exists at api.megam.co.
            |
            |Your email '%s' with an api_key isn't registered to consume api.megam.co. Please register your account in www.megam.co.   
            |Read https://api.megam.co, http://docs.megam.co to know about our API.
            Ask for help on the forums.\n===>\n%s""".format(id, err.map(l => l.getMessage).list.mkString("\n")))
      }

    }

  }
}