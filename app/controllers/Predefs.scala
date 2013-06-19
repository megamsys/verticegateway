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
 * If HMAC authentication is true then post or list the predefs are executed
 *  
 */
object Predefs extends Controller with APIAuthElement {

  /*
   * show the message details
   * 
   */
  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    
    val res = models.Predefs.findByKey(id) match {
      case Success(optAcc) => {
        val foundNode = optAcc.get
        foundNode
      }
      case Failure(_) => None
    }
    println("++++++++++++++Result+++++++++" + res)
    Ok("" + res)
  }

  /*
   * list the particular Id values
   * 
   */
  def list = StackAction(parse.tolerantText) { implicit request =>       
    val valueJson = models.Predefs.listKeys match {
      case Success(t) =>  { 
           t
      }
      case Failure(err) =>
           println("Value fetch failure")        
    }
    println(valueJson)
    Ok("" + valueJson)
  }

  def create = Action { 
    val res = models.Predefs.findAll 
    Ok("Post Action succeeded")
  }
 
}