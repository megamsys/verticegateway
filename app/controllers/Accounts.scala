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
import controllers.stack.APIAuthElement
import controllers.stack._
import java.util.concurrent.atomic.AtomicInteger
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
    models.Accounts.create(input)
    Ok("""Account creation successfully completed.
            |
            |Your email and api_key  registered successully.  Hurray ! Run the other API calls now. 
            |Read https://api.megam.co, http://docs.megam.co for more help. Ask for help on the forums.""")
  }

  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    val res = models.Accounts.findByEmail(id) match {
      case Success(optAcc) => {
        val foundAccount = optAcc.get
        foundAccount
      }
      case Failure(err) => {
        Logger.info("""Your email doesn't exists from megam.co.
            |
            |Please register your account in megam.co.'%s' 
            |""".format(err).stripMargin + "\n ")
      }
    }
    Ok("" + res)
  }

}