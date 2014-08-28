/* 
** Copyright [2013-2014] [Megam Systems]
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
package controllers.funnel

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
//import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }

import controllers.funnel._
import controllers.funnel.FunnelErrors._
import controllers.stack.HeaderConstants._
import controllers.stack.GoofyCrypto._

/**
 * @author ram
 * A Request that comes to megam_play gets funnelled
 *            	 HTTPRequest
 *  		   (Headers, Body)
 *    				 |
 *   			    \ /
 *     				 |
 *   			 (becomes)
 *            FunneledRequest(maybeEmail,clientAPIHmac, clientAPIDate,
 *            clientAPIPath, clientAPIBody)
 */
case class FunneledRequest(maybeEmail: Option[String], clientAPIHmac: Option[String],
  clientAPIDate: Option[String], clientAPIPath: Option[String], clientAPIBody: Option[String]) {

  /**
   * We massage the email to check if it has a valid format
   */
  val wowEmail = {
    val EmailRegex = """^[a-z0-9_\+-]+(\.[a-z0-9_\+-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*\.([a-z]{2,4})$""".r
    maybeEmail.flatMap(x => EmailRegex.findFirstIn(x))
  } match {
    case Some(succ) => Validation.success[Throwable, Option[String]](succ.some)
    case None => Validation.failure[Throwable, Option[String]](new MalformedHeaderError(maybeEmail.get,
      """Email is blank or invalid. Kindly provide us an email in the standard format.\n"
      eg: goodemail@megam.com"""))
  }
  /**
   * Hmm this has created a dependency with GoofyCrypto. Not a good one.
   * This creates a signed string
   * concatenates (date + path + md5ed body) of the content sent via header
   * To do, we append all the Option using ++ and map on it.
   */
  val mkSign = {
    val ab = ((clientAPIDate ++ clientAPIPath ++ calculateMD5(clientAPIBody)) map { a: String => a
    }).mkString("\n")
    play.api.Logger.debug(("%-20s -->[%s]").format("FunnelRequest:mkSign", ab))
    ab
  }

  override def toString = {
    "%-16s%n [%-8s=%s]%n [%-8s=%s]%n [%-8s=%s]%n [%-8s=%s]%n [%-8s=%s]%n".format("FunneledRequest",
      "email", maybeEmail, "apiHMAC", clientAPIHmac, "apiDATE", clientAPIDate,
      "apiPATH", clientAPIPath, "apiBody", clientAPIBody)
  }
}
case class FunnelRequestBuilder[A](req: RequestWithAttributes[A]) {

  private val rawheader = req.headers

  private val clientAPIReqBody = ((req.body).toString()).some
  private val clientAPIReqDate: Option[String] = rawheader.get(X_Megam_DATE)
  private val clientAPIReqPath: Option[String] = req.path.some

  //Look for the X_Megam_HMAC field. If not the FunneledRequest will be None.
  private lazy val frOpt: Option[FunneledRequest] = (for {
    hmac <- rawheader.get(X_Megam_HMAC)
    trimmed <- hmac.trim.some
    res <- trimmed.some
    if (res.indexOf(":") > 0)
  } yield {
    val res1 = res.split(":").take(2)
    FunneledRequest(res1(0).some, res1(1).some, clientAPIReqDate, clientAPIReqPath, clientAPIReqBody)
  })

  /**
   *
   * Start with to see if the FunneledRequest exists, or else send a MalformedHeaderError back.
   * If the FunneledRequest exists, and an invalid email(format - not adhereing to our regex) is present lead to MalformedHeaderError.
   * A valid email exists, then send back a ValidationNel.success with FunneledRequest wrapped in Option.
   */
  def funneled: ValidationNel[Throwable, Option[FunneledRequest]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("FunnelRB:funneled", rawheader.toString))
    play.api.Logger.debug(("%-20s -->[%s]").format("FunnelRB:funneled", frOpt.getOrElse("Funneled Request is NONE! Bummer dude.").toString))

    frOpt match {
      case Some(fr) => fr.wowEmail.leftMap { t: Throwable => t }.toValidationNel.flatMap {
        _: Option[String] => Validation.success[Error, Option[FunneledRequest]](fr.some).toValidationNel
      }
      case None => (Validation.failure[Throwable, Option[FunneledRequest]](
        new MalformedHeaderError(rawheader.get(X_Megam_HMAC).get,
          """We couldn't parse the header. Didn't find %s. """.format(X_Megam_HMAC)))).toValidationNel
    }

  }

}
