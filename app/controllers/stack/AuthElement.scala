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
package controllers.stack

/**
 * @author rajthilak
 *
 */
import scalaz._
import play.api._
import play.api.mvc._
import models._
import controllers.stack._
import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }

/*
 * sub trait for stackable controller,
 * proceed method was override here for our request changes, 
 * And result return in super trait proceed method,
 * when stack action is called then this stackable controller is executed 
 * 
 */
trait AuthElement extends StackableController {

  self: Controller =>

  override def proceed[A](req: RequestWithAttributes[A])(f: RequestWithAttributes[A] => Result): Result = {

    /*
     * If HMAC authentication is true, the req send in super class
     * otherwise badrequest return   
     */

    SecurityActions.Authenticated(req) match {
      case Success(msgs) =>
        BadRequest
      case Failure(msg) => super.proceed(req)(f)
    }

  }

}

