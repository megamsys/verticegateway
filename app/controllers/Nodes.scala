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
import org.megam.common.amqp._
import scalaz.Validation._
import play.api.mvc.Result

/**
 * @author ram
 *
 */

/*
 * this controller for HMAC authentication and access riak
 * If HMAC authentication is true then post or list the nodes are executed
 *  
 */
object Nodes extends Controller with APIAuthElement with Helper {

  /*
   * parse.tolerantText to parse the RawBody 
   * get requested body and put into the riak bucket
   */
  def post = StackAction(parse.tolerantText) { implicit request =>
    val input = (request.body).toString()
    val sentHmacHeader = request.headers.get(HMAC_HEADER);
    val id = getAccountID(sentHmacHeader)
    models.Nodes.create(input, id)
    Ok("""Node creation successfully completed.
            |
            |Your node created successully.  Try other node's for your account. 
            |Read https://api.megam.co, http://docs.megam.co for more help. Ask for help on the forums.""")
  }

  /*
   * show the message details
   * 
   */
  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    val res = models.Nodes.findByKey(id) match {
      case Success(optAcc) => {
        val foundNode = optAcc.get
        foundNode
      }
      case Failure(err) => {
        Logger.info("""In this account doesn't create in this '%s' nodes 
            |
            |Please create new Node for your Account 
            |Read https://api.megam.co, http://docs.megam.co for more help. Ask for help on the forums.""".format(id).stripMargin
          + "\n" + apiAccessed)
      }
    }

    Ok("" + res)
  }

  /*
   * list the particular Id values
   * 
   */
  def list = StackAction(parse.tolerantText) { implicit request =>
    val input = (request.body).toString()
    val sentHmacHeader = request.headers.get(HMAC_HEADER);
    val id = getAccountID(sentHmacHeader)
    val valueJson = models.Nodes.findById(id) match {
      case Success(v) => {
        //val m = v.get
        //m.predefs
        v
      }
      case Failure(err) => {
        Logger.info("""In this account doesn't create any nodes --> '%s'
            |
            |Please create new Nodes in your Account 
            |Read https://api.megam.co, http://docs.megam.co for more help. Ask for help on the forums.""".format(err).stripMargin
          + "\n" + apiAccessed)
      }
    }    
    Ok("" + valueJson)
  }
}