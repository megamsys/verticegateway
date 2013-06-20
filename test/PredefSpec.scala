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


class PredefSpec extends Specification {
   def is =
    "PredefSpec".title ^ end ^
      """
HMACSpec is the implementation that calls the megam_play API server with the /nodes url to verify HMAC only
    """ ^ end ^
      "The Client Should" ^
      "Correctly do GET requests" ! Get().succeeds ^
      end

  trait Context extends BaseContextGet {

    //create htttp client
    val httpClient = new ApacheHttpClient
    
    //protected lazy val url = new URL("http://localhost:9000/v1/accounts/sandy@megamsandbox.com")
    protected lazy val url = new URL("http://localhost:9000/v1/predefs")
   
    //val headerAndBody = sandboxHeaderAndBody(contentToEncode, url.getPath)
    val headerAndBody = sandboxHeaderAndBody(url.getPath)
    
    protected val headers = headerAndBody._1
    //protected val body = headerAndBody._2

    protected def execute[T](t: Builder, expectedCode: HttpResponseCode = HttpResponseCode.Ok)(fn: HttpResponse => MatchResult[T]) = {

      val r = t.executeUnsafe
      r.code must beEqualTo(expectedCode) and fn(r)
    }

    implicit private val encoding = Constants.UTF8Charset

    protected def ensureHttpOk(h: HttpResponse) = h.code must beEqualTo(HttpResponseCode.Ok)
  }

  //post the headers and their body for specifing url
  case class Get() extends Context {
    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = execute(get)(ensureHttpOk(_))
  }
  
}