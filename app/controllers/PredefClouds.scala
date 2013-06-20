/* 
** Copyright [2012] [Megam Systems]
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
import org.megam.common.amqp._
import scalaz.Validation._
import play.api.mvc.Result

/**
 * @author rajthilak
 *
 */

/*
 * this controller for HMAC authentication and access riak
 * If HMAC authentication is true then post or list the predefs clouds are executed
 *  
 */
object PredefClouds extends Controller with APIAuthElement with NodesHelper {

  /*
   * parse.tolerantText to parse the RawBody 
   * get requested body and put into the riak bucket
   */
  def post = Action(parse.tolerantText) { implicit request =>
    val input = (request.body).toString()
    val sentHmacHeader = request.headers.get(HMAC_HEADER);
    val id = getAccountID(sentHmacHeader)
    models.PredefClouds.create(input, id)
    Ok("Post Action succeeded")
  }
 
  
}