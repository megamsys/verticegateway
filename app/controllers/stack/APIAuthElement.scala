/*
** Copyright [2013-2015] [Megam Systems]
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
import scala.concurrent.Future

import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }

import controllers.Constants._
import controllers.funnel._
import controllers.funnel.FunnelErrors._
import play.api.mvc._
import play.api.libs.iteratee.Enumerator

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

  case object APIAccessedKey extends RequestAttributeKey[Option[String]]

  /**
   * If HMAC authentication is true, the req send in super class
   * otherwise send out a json formatted error
   */
  override def proceed[A](req: RequestWithAttributes[A])(f: RequestWithAttributes[A] => Future[Result]): Future[Result] = {
    play.api.Logger.debug("%s%s====> %s%s%s ".format(Console.CYAN, Console.BOLD, req.host, req.path, Console.RESET))
    play.api.Logger.debug("%s%sHEAD:%s %s%s%s".format(Console.MAGENTA, Console.BOLD, Console.RESET, Console.BLUE, req.headers, Console.RESET))
      play.api.Logger.debug("%s%sBODY:%s %s%s%s\n".format(Console.MAGENTA, Console.BOLD, Console.RESET, Console.BLUE, req.headers, Console.RESET))
    SecurityActions.Authenticated(req) match {
      case Success(rawRes) => super.proceed(req.set(APIAccessedKey, rawRes))(f)
      case Failure(err) => {
        val g = Action { implicit request =>
          val rn: FunnelResponse = new HttpReturningError(err) //implicitly loaded.
          Result(header = ResponseHeader(rn.code, Map(CONTENT_TYPE -> "text/plain")),
            body = Enumerator(rn.toJson(true).getBytes(UTF8Charset)))
        }
        val origReq = req.asInstanceOf[Request[AnyContent]]
        g(origReq)
      }

    }
  }

  implicit def reqFunneled[A](implicit req: RequestWithAttributes[A]): ValidationNel[Throwable, Option[FunneledRequest]] = req2FunnelBuilder(req).funneled

  implicit def apiAccessed[A](implicit req: RequestWithAttributes[A]): Option[String] = req.get(APIAccessedKey).get

}
