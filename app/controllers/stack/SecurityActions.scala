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
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.NonEmptyList._

import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import org.apache.commons.codec.binary.Base64
import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }

import controllers.stack.stack._
import controllers.funnel._
import controllers.funnel.FunnelErrors._
import models.Accounts
import models.AccountResult

import play.api.mvc._
import play.api.http.Status._
import play.api.Logger
/**
 * @author rajthilak
 *
 */

object SecurityActions {

  val X_Megam_EMAIL = "X-Megam-EMAIL"
  val X_Megam_APIKEY = "X-Megam-APIKEY"
  val X_Megam_DATE = "X-Megam-DATE"
  val X_Megam_HMAC = "X-Megam-HMAC"

  val Content_Type = "Content-Type"
  val application_json = "application/json"
  val Accept = "Accept"
  val application_vnd_megam_json = "application/vnd.megam+json"

  def Authenticated[A](req: FunnelRequestBuilder[A]): ValidationNel[Throwable, Option[String]] = {
    Logger.debug(("%-20s -->[%s]").format("SecurityActions", "Authenticated:Entry"))
    req.funneled match {
      case Success(succ) => {
        Logger.debug(("%-20s -->[%s]").format("FUNNLEDREQ-S", succ.toString))
        (succ map (x => bazookaAtDataSource(x))).getOrElse(
          Validation.failure[Throwable, Option[String]](CannotAuthenticateError("""Invalid content in header. API server couldn't parse it""",
            "Request can't be funneled.")).toValidationNel)

      }
      case Failure(err) =>
        val errm = (err.list.map(m => m.getMessage)).mkString("\n")
        Logger.debug(("%-20s -->[%s]").format("FUNNLEDREQ-F", errm))
        Validation.failure[Error, Option[String]](CannotAuthenticateError(
          """Invalid content in header. API server couldn't parse it.""", errm)).toValidationNel
    }
  }

  /**
   * This Authenticated function will extract information from the request and calculate
   * an HMAC value. The request is parsed as tolerant text, as content type is application/json,
   * which isn't picked up by the default body parsers in the controller.
   * If the header exists then
   * the string is split on : and the header is parsed
   * else
   */
  def bazookaAtDataSource(freq: FunneledRequest): ValidationNel[Throwable, Option[String]] = {
    play.api.Logger.debug("<O==>------------------------------------->")
    (for {
      resp <- eitherT[IO, NonEmptyList[Throwable], Option[AccountResult]] { //disjunction Throwabel \/ Option with a Function IO.
        (Accounts.findByEmail(freq.maybeEmail.get).disjunction).pure[IO]
      }
      found <- eitherT[IO, NonEmptyList[Throwable], Option[String]] {
        val fres = resp.get
        val calculatedHMAC = GoofyCrypto.calculateHMAC(fres.api_key, freq.mkSign)
        if (calculatedHMAC === freq.clientAPIHmac.get) {
          (("""Authorization successful for 'email:' HMAC matches:
            |%-10s -> %s
            |%-10s -> %s
            |%-10s -> %s""".format("email", fres.email, "api_key", fres.api_key, "authority", fres.authority).stripMargin)
            .some).right[NonEmptyList[Error]].pure[IO]
        } else {
          (nels((CannotAuthenticateError("""Authorization failure for 'email:' HMAC doesn't match: '%s'."""
            .format(fres.email).stripMargin, "", UNAUTHORIZED))): NonEmptyList[Error]).left[Option[String]].pure[IO]
        }
      }
    } yield found).run.map(_.validation).unsafePerformIO()
  }
}

/**
 * GoofyCrypto just provides methods to make a content into MD5,
 * calculate a HMACSHA1, using a RAW secret (api_key). -- TO-DO change the api_key as SHA1.
 */
object GoofyCrypto {
  /**
   * Calculate the MD5 hash for the specified content (UTF-16 encoded)
   */
  def calculateMD5(content: Option[String]): Option[String] = {
    val MD5 = "MD5"
    Logger.debug(("%-20s -->[%s]").format("MD5 CONTENT", content.get))
    val digest = MessageDigest.getInstance(MD5)
    digest.update(content.getOrElse(new String()).getBytes)
    val md5b = new String(Base64.encodeBase64(digest.digest()))
    Logger.debug(("%-20s -->[%s]").format("MD5 OUTPUT", md5b))
    md5b.some
  }

  /**
   * Calculate the HMAC for the specified data and the supplied secret (UTF-16 encoded)
   */
  def calculateHMAC(secret: String, toEncode: String): String = {
    val HMACSHA1 = "HmacSHA1"
    Logger.debug(("%-20s -->[%-20s,%s]").format("HMAC: ENTRY", secret, toEncode))

    val signingKey = new SecretKeySpec(secret.getBytes(), "RAW")
    val mac = Mac.getInstance(HMACSHA1)
    mac.init(signingKey)
    val rawHmac = mac.doFinal(toEncode.getBytes())
    val hmacAsByt = dumpByt(rawHmac.some)
    hmacAsByt
  }

  def dumpByt(bytesOpt: Option[Array[Byte]]): String = {
    val b: Array[String] = (bytesOpt match {
      case Some(bytes) => bytes.map(byt => (("00" + (byt &
        0XFF).toHexString)).takeRight(2))
      case None => Array(0X00.toHexString)
    })
    b.mkString("")
  }

}

