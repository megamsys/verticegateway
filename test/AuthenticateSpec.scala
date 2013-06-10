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

/**
 * @author rajthilak
 *
 */

class AuthenticateSpec extends Specification {

  def is =
    "AuthenticateSpec".title ^ end ^
      """
  AuthenticateSpec is the implementation that calls the megam_play API server with the /auth url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do POST requests" ! Post().succeeds ^
      end

  trait Context extends BaseContext {

    //create htttp client
    val httpClient = new ApacheHttpClient

    protected lazy val url = new URL("http://localhost:9000/v1/auth ")

    //create the contentToEncode as request Body
    //val contentToEncode = "{\"comment\" : {\"message\":\"blaat\" , \"from\":\"blaat\" , \"commentFor\":123}}"

    //val headerAndBody = sandboxHeaderAndBody(contentToEncode, url.getPath)
    val headerAndBody = sandboxHeaderAndBody(url.getPath)
    protected val headers = headerAndBody._1
    protected val body = headerAndBody._2

    protected def execute[T](t: Builder, expectedCode: HttpResponseCode = HttpResponseCode.Ok)(fn: HttpResponse => MatchResult[T]) = {
      val r = t.executeUnsafe
      r.code must beEqualTo(expectedCode) and fn(r)
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