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

import scalaz._
import Scalaz._
import scalaz.Validation._
import play.api._
import play.api.mvc.Request
import play.api.Logger
import play.api.mvc._
import play.api.libs.iteratee.Enumerator

import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }

import controllers.stack._
import models._

/**
 * @author rajthilak
 *
 */
/*
 * sub trait for stackable controller, proceed method was override here for our request changes, 
 * And result return in super trait proceed method,
 * when stack action is called then this stackable controller is executed  
 */
trait APIAuthElement extends StackableController {

  self: Controller =>

  case object APIAccessedKey extends RequestAttributeKey[RawResult]

  /**
   * If HMAC authentication is true, the req send in super class
   * otherwise badrequest return
   */
  override def proceed[A](req: RequestWithAttributes[A])(f: RequestWithAttributes[A] => Result): Result = {

    Logger.debug("APIAuthElement :" + req)
    SecurityActions.Authenticated(req) match {
      case Success(rawRes) => super.proceed(req.set(APIAccessedKey, rawRes))(f)
      case Failure(err) => {
        val g = Action { implicit request =>
          SimpleResult(header = ResponseHeader(err.head.get._1, Map(CONTENT_TYPE -> "text/plain")),
            body = Enumerator(err.head.get._2))
        }
        val origReq = req.asInstanceOf[Request[AnyContent]]
        g(origReq)
      }

    }
  }

  implicit def apiAccessed[A](implicit req: RequestWithAttributes[A]): RawResult = req.get(APIAccessedKey).get

}

