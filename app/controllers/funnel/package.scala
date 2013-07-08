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
import scalaz.Validation
import controllers.funnel.FunnelErrors._
import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }

/**
 * @author ram
 *
 */
package object funnel {

  type FunneledHeader = Option[String]
  type FunneledBody = Option[String]

  implicit def req2FunnelBuilder[A](req: RequestWithAttributes[A]): FunnelRequestBuilder[A] = new FunnelRequestBuilder[A](req)

  implicit class RichThrowable(thrownExp: Throwable) {
    def fold[T](
      cannotAuthError: CannotAuthenticateError => T,
      malformedBodyError: MalformedBodyError => T,
      malformedHeaderError: MalformedHeaderError => T,
      serviceUnavailableError: ServiceUnavailableError => T,
      resourceNotFound: ResourceItemNotFound => T,
      anyError: Throwable => T): T = thrownExp match {
      case a @ CannotAuthenticateError(_, _, _) => cannotAuthError(a)
      case m @ MalformedBodyError(_, _, _)      => malformedBodyError(m)
      case h @ MalformedHeaderError(_, _, _)    => malformedHeaderError(h)
      case c @ ServiceUnavailableError(_, _, _) => serviceUnavailableError(c)
      case r @ ResourceItemNotFound(_, _, _)    => resourceNotFound(r)
      case t @ _                                => anyError(t)
    }
  }

}