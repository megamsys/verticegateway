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
/**
 * @author rajthilak
 *
 */
package test

import com.stackmob.newman._
import com.stackmob.newman.response._
import org.specs2.matcher.{ MatchResult, Expectable, Matcher }
import org.specs2.execute.{ Failure => SpecsFailure, Result => SpecsResult }
import scalaz._
import Scalaz._
import net.liftweb.json.scalaz.JsonScalaz._
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import org.apache.commons.codec.binary.Base64
import com.stackmob.newman._
import java.util.Calendar
import java.text.SimpleDateFormat


trait BaseContext {

  val MD5 = "MD5"
  val HMACSHA1 = "HmacSHA1"
  val sandbox_secret="IamAtlas{70}NobodycanSeeME#04"
  val sandbox_email ="sanboxemail@megamsanbox.com"
    
  protected class HeadersAreEqualMatcher(expected: Headers) extends Matcher[Headers] {
    override def apply[S <: Headers](r: Expectable[S]): MatchResult[S] = {
      val other: Headers = r.value
      val res = expected === other
      result(res, "Headers are equal", expected + " does not equal " + other, r)
    }
  }

  protected class HttpResponsesAreEqualMatcher(expected: HttpResponse) extends Matcher[HttpResponse] {
    override def apply[S <: HttpResponse](r: Expectable[S]): MatchResult[S] = {
      val other: HttpResponse = r.value
      val res = (expected.code === other.code) && (expected.headers === other.headers) && (expected.bodyString === other.bodyString)
      result(res, "HttpResponses are equal", expected + " does not equal " + other, r)
    }
  }

  protected def haveTheSameHeadersAs(h: Headers) = new HeadersAreEqualMatcher(h)

  protected def beTheSameResponseAs(h: HttpResponse) = new HttpResponsesAreEqualMatcher(h)

  protected def logAndFail(t: Throwable): SpecsResult = {
    SpecsFailure("failed with exception %s".format(t.getMessage))
  }

  private def errorString(err: Error) = err match {
    case UnexpectedJSONError(was, expected)  => "unexpected JSON. was %s, expected %s".format(was.toString, expected.toString)
    case NoSuchFieldError(name, json)        => "no such field %s in json %s".format(name, json.toString)
    case UncategorizedError(key, desc, args) => "uncategorized JSON error for key %s: %s (args %s)".format(key, desc, args.mkString("&"))
  }

  protected def logAndFail(errs: NonEmptyList[Error]): SpecsResult = {
    val totalErrString = errs.map(errorString(_)).list.mkString("\n")
    SpecsFailure("JSON errors occurred: %s".format(totalErrString))
  }

  protected def calculateHMAC(secret: String, data: String): String = {
    val signingKey = new SecretKeySpec(secret.getBytes(), HMACSHA1)
    val mac = Mac.getInstance(HMACSHA1)
    mac.init(signingKey)
    val rawHmac = mac.doFinal(data.getBytes())
    new String(Base64.encodeBase64(rawHmac))
  }

  protected def calculateMD5(content: String): String = {
    val digest = MessageDigest.getInstance(MD5)
    digest.update(content.getBytes())
    new String(Base64.encodeBase64(digest.digest()))
  }
  
  protected def sandboxHeaderAndBody(contentToEncode: String, path: String):(Headers, RawBody) = {
     //create the contentToEncode as request Body
    val contentToEncode = "{\"id\":\"2\", \"email\":\"chris@example.com\", \"sharedprivatekey\":\"secret\", \"authority\":\"user\" }"
    val contentType = "application/json"
    val currentDate = new SimpleDateFormat("yyy-MM-dd HH:mm") format Calendar.getInstance.getTime

    // create the string that we'll have to sign   
    val signWithHMAC = currentDate + "\n" + path + "\n" + calculateMD5(contentToEncode)

    //calculate the HMAC value using "user secret key" and "toSign" values
    val signedWithHMAC = calculateHMAC(sandbox_secret, signWithHMAC)

    //set Headers using hmac, date and content type 
    val finalHMAC = sandbox_email + ":" + signedWithHMAC
    (Headers("content-type" -> contentType,
      "hmac" -> finalHMAC,
      "date" -> currentDate), RawBody(contentToEncode))
  }
}