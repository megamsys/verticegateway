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

package test

import org.specs2.mutable._
import org.specs2.Specification
import java.net.URL
import org.specs2.matcher.MatchResult
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import org.apache.commons.codec.binary.Base64
import java.util.Calendar
import java.text.SimpleDateFormat

/**
 * @author rajthilak
 *
 */

class SourceSpec extends Specification {

  def is =
    "ApacheHttpClientSpecs".title ^ end ^
      """
  ApacheHttpClient is the HttpClient implementation that actually hits the internet
  """ ^ end ^
      "The Client Should" ^
      // "Correctly do GET requests" ! Get().succeeds ^
      "Correctly do POST requests" ! Post().succeeds ^      
      end

  val MD5 = "MD5"
  val HMACSHA1 = "HmacSHA1"

  trait Context extends BaseContext {

    //create htttp client
    val httpClient = new ApacheHttpClient
    protected lazy val url = new URL("http://localhost:9000/accounts/content")
    //protected lazy val url = new URL("http://localhost:9000/v1/logs")
    
    //create the contentToEncode as request Body
    val contentToEncode = "{\"id\":\"2\", \"email\":\"chris@example.com\", \"sharedprivatekey\":\"secret\", \"authority\":\"user\" }"

    //this is request headers and body http content type's    
    val contentType = "application/vnd.geo.comment+json"

    //get the current date and change date format  
    val formatString = "yyy-MM-dd HH:mm"
    val cal = Calendar.getInstance
    val currentDate = new SimpleDateFormat(formatString) format cal.getTime

    //this is the user's secret key
    private final val SECRET = "secret"

    //content added to RawBody with encode at default UTF8  
    protected val body = RawBody(contentToEncode)

    val contentMd5 = calculateMD5(contentToEncode)

    // create the string that we'll have to sign   
    val toSign = currentDate + "\n" + url.getPath() + "\n" + contentMd5

    //calculate the HMAC value using "user secret key" and "toSign" values
    val hmac = calculateHMAC(SECRET, toSign)

    //set Headers using hmac, date and content type 
    val userHMAC = "chris@example.com:" + hmac
    protected val headers = Headers("content-type" -> "application/vnd.geo.comment+json",
      "hmac" -> userHMAC,
      "date" -> currentDate)

    protected def execute[T](t: Builder, expectedCode: HttpResponseCode = HttpResponseCode.Ok)(fn: HttpResponse => MatchResult[T]) = {

      val r = t.executeUnsafe

      r.code must beEqualTo(expectedCode) and fn(r)
    }

    /**
     * Calculate the MD5 hash for the specified content
     */
    private def calculateHMAC(secret: String, data: String): String = {
      val signingKey = new SecretKeySpec(secret.getBytes(), HMACSHA1)
      val mac = Mac.getInstance(HMACSHA1)
      mac.init(signingKey)
      val rawHmac = mac.doFinal(data.getBytes())
      new String(Base64.encodeBase64(rawHmac))
    }

    /**
     * Calculate the HMAC for the specified data and the supplied secret
     */
    private def calculateMD5(content: String): String = {
      val digest = MessageDigest.getInstance(MD5)
      digest.update(content.getBytes())
      new String(Base64.encodeBase64(digest.digest()))
    }

    implicit private val encoding = Constants.UTF8Charset

    protected def ensureHttpOk(h: HttpResponse) = h.code must beEqualTo(HttpResponseCode.Ok)
  }

  //post the headers and their body for specifing url
  case class Post() extends Context {
    private val post = POST(url)(httpClient)
      .addHeaders(headers)
      .addBody(body)
    def succeeds = execute(post)(ensureHttpOk(_))
  }
}