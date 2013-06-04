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
import scalaz.Validation._
import play.api.mvc.Action
import play.api.Logger
import play.api.mvc.RequestHeader
import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.mvc.Result
import play.api.mvc.RawBuffer
import play.api.mvc.Codec
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import org.apache.commons.codec.binary.Base64
import net.liftweb.json._
import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }
import models.{ Accounts, DomainObjects }

/**
 * @author rajthilak
 *
 */

case class AccountJson(id: String, email: String, api_key: String, authority: String)

object SecurityActions {

  val HMAC_HEADER = "hmac"
  val CONTENT_TYPE_HEADER = "content-type"
  val DATE_HEADER = "date"
  val MD5 = "MD5"
  val HMACSHA1 = "HmacSHA1"

  implicit val formats = DefaultFormats

  /**
   * Function authenticated is defined as a function that takes as parameter a function which takes as argument  a user and a request. The authenticated
   * function itself, returns a result.
   *
   * This Authenticated function will extract information from the request and calculate
   * an HMAC value.
   * The request is parsed as tolerant text, as content type is application/json, which isn't picked
   * up by the default body parsers in the controller. Alternative would be to parse the RawBuffer manually get the header we're working with
   *
   * Need to change the return of this method as ValidationNel (success/failure)
   */
  def Authenticated[A](req: RequestWithAttributes[A]): ValidationNel[String, String] = {

    val sentHmacHeader = req.headers.get(HMAC_HEADER);

    // Check whether we've recevied an hmac header
    sentHmacHeader match {

      // if we've got a value that looks like our header 
      case Some(x) if x.contains(":") && x.split(":").length == 2 => {

        // first part is username, second part is hash
        val headerParts = x.split(":");

        // Retrieve all the headers we're going to use, we parse the complete 
        // content-type header, since our client also does this
        val input = List(
          req.headers.get(DATE_HEADER),
          req.path,
          calculateMD5((req.body).toString()))

        // create the string that we'll have to sign       
        val toSign = input.map(
          a => {
            a match {
              case None           => ""
              case a: Option[Any] => a.asInstanceOf[Option[Any]].get
              case _              => a
            }
          }).mkString("\n")
        Logger.debug("sign         =>" + toSign)

        // use the input to calculate the hmac        
        // if the supplied value and the received values are equal
        // return the response from the delegate action, else return
        // unauthorized           
        val db = DomainObjects.clientCreate()
        val authmaybe = models.Accounts.findById(db, "accounts", "content1")
        authmaybe match {
          case Some(acc) => {
            val json = parse(acc.value)
            val m = json.extract[AccountJson]
            val calculatedHMAC = calculateHMAC(m.api_key, toSign)
            Logger.debug("hmac         =>" + calculatedHMAC + ",[" + m.api_key + "," + headerParts(1) + "]")
            //check calculated HMAC value and response HMAC value 
            //If this check also included user's api_key key
            if (calculatedHMAC == headerParts(1)) {
              Validation.success[String, String]("""Authorization successful for 'email:' HMAC matched: '%s'
            |
            |Your email and api_key  combination was verified successuly.  Try other API invocation. 
            |Read https://api.megam.co, http://docs.megam.co for more help. Ask for help on the forums.""".format(m.email).stripMargin + "\n ").toValidationNel

            } else {
              Validation.failure[String, String]("""Authorization failure for 'email:' HMAC doesn't match: '%s'
            |
            |Please verify your email and api_key  combination. This  needs to  appear  as-is  during onboarding
            |from the megam.co webiste. If this error persits, ask for help on the forums.""".format(m.email).stripMargin + "\n ").toValidationNel
            }
          }
          case None => {
            Validation.failure[String, String]("""Autorization failure for 'email:' Couldn't locate the 'email':'
            |
            |Please verify your email and api_key  combination. This  needs to  appear  as-is  during onboarding
            |from the megam.co webiste. If this error persits, ask for help on the forums.""".stripMargin + "\n ").toValidationNel
          }
        }
      }
      // All the other possibilities return to 401 
      case _ => {
        Validation.failure[String, String]("""Autorization failure for 'email:' Invalid content in header. API server couldn't parse it.:'
            |
            |Please verify your email and api_key  combination. This  needs to  appear  as-is  during onboarding
            |from the megam.co webiste. This is a bug in the API client. If you have accessed   this   using our
            |api code (megam_api for ruby, scala, java etc..) then PLEASE LOG A JIRA ISSUE.""".stripMargin + "\n ").toValidationNel

      }
    }
  }
  /**
   * Calculate the MD5 hash for the specified content
   */
  private def calculateMD5(content: String): String = {
    Logger.debug("body content =>" + content)
    val digest = MessageDigest.getInstance(MD5)
    digest.update(content.getBytes())
    Logger.debug("body digest  =>" + digest)
    new String(Base64.encodeBase64(digest.digest()))
  }

  /**
   * Calculate the HMAC for the specified data and the supplied secret
   */
  private def calculateHMAC(secret: String, toEncode: String): String = {
    val signingKey = new SecretKeySpec(secret.getBytes(), HMACSHA1)
    val mac = Mac.getInstance(HMACSHA1)
    mac.init(signingKey)
    val rawHmac = mac.doFinal(toEncode.getBytes())
    new String(Base64.encodeBase64(rawHmac))
  }

}   
 