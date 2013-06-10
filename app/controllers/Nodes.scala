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
/**
 * @author ram
 *
 */

/*
 * this controller for HMAC authentication and access riak
 * If HMAC authentication is true then post or list the nodes are executed
 *  
 */
object Nodes extends Controller with APIAuthElement with SourceElement {

  /*
   * parse.tolerantText to parse the RawBody 
   * get requested body and put into the riak bucket
   */
  def post = StackAction(parse.tolerantText) { implicit request =>
    val input = (request.body).toString()
    models.Nodes.create("key1", input)
    Ok("Post Action succeeded")
  }

  def create = StackAction(parse.tolerantText) { implicit request =>
    val result = models.Nodes.findByEmail("email@id")
    result match {
      case Success(node) => {
        //    MessageObjects.Publish(node.key).succeeds
        //   Ok("Nodes Page succeeded ========>" + node.key + "   :   " + node.value)
        Ok("Nodes Not Implemented.")
      }
      case Failure(node) =>
        Ok("Key not Found")
    }
  }

  /*
   * show the message details
   * 
   */
  def show(id: Long) = StackAction { implicit request =>
    val title = "messages detail "
    Ok(views.html.index(title + id))
  }

  /*
   * list the particular Id values
   * 
   */
  def list = StackAction(parse.tolerantText) { implicit request =>
    val result = models.Nodes.findById("content1")
    result match {
      case Success(node) => {
        Ok("Nodes Page succeeded ========>")
      }
      case Failure(node) =>
        Ok("Key not Found")
    }
  }

}

