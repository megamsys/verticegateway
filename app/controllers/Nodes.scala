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
object Nodes extends Controller with APIAuthElement {

  val HMAC_HEADER = "hmac"
  /*
   * parse.tolerantText to parse the RawBody 
   * get requested body and put into the riak bucket
   */
  def post = Action(parse.tolerantText) { implicit request =>
    val input = (request.body).toString()
    val sentHmacHeader = request.headers.get(HMAC_HEADER);
    val email: String = sentHmacHeader match {
      case Some(x) if x.contains(":") && x.split(":").length == 2 => {
        val headerParts = x.split(":")
        headerParts(0)
      }
      case _ =>  ""
    }
    val id = models.Accounts.findByEmail(email) match {
      case Success(optAcc) => {
        val foundAccount = optAcc.get
        foundAccount.id
      }
      case Failure(_) => ""
    }
    println("+++++++++++++++++++++++++++++"+id)
    models.Nodes.create(input, id)
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
    val acc = models.Accounts.findByEmail("sandy@megamsandbox.com")
    acc match {
      case Success(optAcc) => {
        val foundAccount = optAcc.get
        val result = models.Nodes.findByEmail(foundAccount.id)
        result match {
          case Success(optNod) => {
            val nodClass = optNod.get
            println("Nodes value---" + nodClass.acc_id)
            Ok("Nodes value---" + nodClass.acc_id)
          }
          case Failure(err) => {
            println("Error------" + err)
            Ok("Error------" + err)
          }
        }
      }
      case Failure(err1) => {
        println("Error------" + err1)
        Ok("Error------" + err1)
      }
    }
  }

}

