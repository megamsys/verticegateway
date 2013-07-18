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
import scalaz.NonEmptyList._
import scalaz.Validation
import controllers.funnel.FunnelErrors._
import controllers.funnel.FunnelResponsesSerialization

import controllers.Constants._

import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }
import play.api.http.Status._
import java.io.{ StringWriter, PrintWriter }
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author ram
 *
 */
package object funnel {

  type FunneledHeader = Option[String]
  type FunneledBody = Option[String]
  type FunnelResponseList = NonEmptyList[FunnelResponse]
  type FunnelResponses = Option[FunnelResponseList]

  object FunnelResponses {

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(fres: FunnelResponses): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import controllers.funnel.FunnelResponsesSerialization.{ writer => FunResponsesWriter }
      toJSON(fres)(FunResponsesWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(fres: FunnelResponses, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(fres)))
    } else {
      compactRender(toJValue(fres))
    }

    def apply(f: FunnelResponse): FunnelResponses = FunnelResponses(nels(f))
    def apply(f: FunnelResponseList): FunnelResponses = f.some
    def apply(f: List[FunnelResponse]): FunnelResponses = f.toNel
    def empty: FunnelResponses = Option.empty[FunnelResponseList]
  }

  implicit def req2FunnelBuilder[A](req: RequestWithAttributes[A]): FunnelRequestBuilder[A] = new FunnelRequestBuilder[A](req)
  /**
   * Broadly erros are categorized into
   * 1. Authentication errors 2. Malformed Inputs 3. Service Errors 4. Resource Errors 5. JSON errors
   * JSON erros aren't traked yet
   */
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

  implicit def err2FunnelResponse(hpret: HttpReturningError) = new FunnelResponse(hpret.code.getOrElse(BAD_REQUEST), hpret.getMessage, hpret.more.getOrElse(new String()), hpret.severity)

  implicit def err2FunnelResponses(hpret: HttpReturningError) = hpret.errNel.map { err: Throwable =>
    err.fold(a => new FunnelResponse(hpret.mkCode(a).getOrElse(BAD_REQUEST), hpret.mkMsg(a), hpret.mkMore(a), hpret.severity),
      m => new FunnelResponse(hpret.mkCode(m).getOrElse(BAD_REQUEST), hpret.mkMsg(m), hpret.mkMore(m), hpret.severity),
      h => new FunnelResponse(hpret.mkCode(h).getOrElse(BAD_REQUEST), hpret.mkMsg(h), hpret.mkMore(h), hpret.severity),
      c => new FunnelResponse(hpret.mkCode(c).getOrElse(BAD_REQUEST), hpret.mkMsg(c), hpret.mkMore(c), hpret.severity),
      r => new FunnelResponse(hpret.mkCode(r).getOrElse(BAD_REQUEST), hpret.mkMsg(r), hpret.mkMore(r), hpret.severity),
      t => new FunnelResponse(hpret.mkCode(t).getOrElse(BAD_REQUEST), hpret.mkMsg(t), hpret.mkMore(t), hpret.severity))
  }.some
  
  implicit def funnelResponses2Json(fres: FunnelResponses): String = FunnelResponses.toJson(fres, true)

}