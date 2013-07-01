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
import org.specs2.execute.{ Result => SpecsResult }

import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import org.apache.commons.codec.binary.Base64
import java.util.Calendar
import java.text.SimpleDateFormat

class PredefCloudSpec extends Specification {
  def is =
    "PredefCloudSpec".title ^ end ^
      """
PredefCloudSpec is the implementation that calls the megam_play API server with the /predefcloud url to create predefclouds
    """ ^ end ^
      "The Client Should" ^
      "Correctly do POST requests" ! Post.succeeds ^
      end

  //post the headers and their body for specifing url
  case object Post extends Context {

    protected override def urlSuffix: String = "predefclouds/<put_the_email_here>"

    private val post = POST(url)(httpClient)
      .addHeaders(headers)
      .addBody(body)
    def succeeds: SpecsResult = {
      val resp = execute(post)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

}
  