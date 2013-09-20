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

import scala.concurrent.Future
import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }
import models._
import play.api._
import play.api.mvc._
import play.api.mvc.{ SimpleResult, Controller }

/**
 * @author rajthilak
 *
 */

/*
 * Used it to perform rate limiting
 * TO-DO: Yet to me implemented. 
 * 
 */
trait RateLimitElement extends StackableController {

  self: Controller =>

  case object RateLimitKey extends RequestAttributeKey[String]

  override def proceed[A](req: RequestWithAttributes[A])(f: RequestWithAttributes[A] => Future[SimpleResult]): Future[SimpleResult] = {
    /*
    * How do we do that ? If the api_cursor_bucket for an user is < X calls then proceed, or dump out 
    * a result as we did in APIAuthElement 
    
    RateLimitCursor.clientCreate() match {
      case db => super.proceed(req.set(RateLimitKey, ""))(f)
      case _  => BadRequest
    }*/
    Future.successful(BadRequest)
  }

  implicit def rateLimitImplicit[A](implicit req: RequestWithAttributes[A]): String = req.get(RateLimitKey).get
}