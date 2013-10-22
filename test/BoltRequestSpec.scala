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
package test

import org.specs2.mutable._
import org.specs2.Specification
import java.net.URL
import org.specs2.matcher.MatchResult
import org.specs2.execute.{ Result => SpecsResult }
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import models._

class BoltRequestSpec extends Specification {

  def is =
    "BoltRequestSpec".title ^ end ^ """
  BoltRequestSpec is the implementation that calls the megam_play API server with the /requests url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do POST requests with a valid userid and api key" ! Post.succeeds ^
      "Correctly do GET  (node name)requests with a invalid Node name" ! findByInvalidName.succeeds ^
      "Correctly do GET  (node name)requests with a valid node name" ! findByName.succeeds ^
      end

  /**
   * Change the body content in method bodyToStick
   */
  case object Post extends Context {

    protected override def urlSuffix: String = "boltreqs/content"

    protected override def bodyToStick: Option[String] = {      
      val contentToEncode = "{\"req_type\":\"NSTART\",\"node_name\":\"checktest4.megam.co\",\"boltdefns_id\":\"BDF392631536735748096\",\"lc_apply\":\"lc_apply\",\"lc_additional\":\"lc_additional\",\"lc_when\":\"lc_when\"}"                                 
      Some(new String(contentToEncode))
    }
    protected override def headersOpt: Option[Map[String, String]] = None

    private val post = POST(url)(httpClient)
      .addHeaders(headers)
      .addBody(body)

    def succeeds: SpecsResult = {
      val resp = execute(post)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Created)
    }
  }

  case object findByInvalidName extends Context {
    protected override def urlSuffix: String = "boltreqs/checksample"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }
  case object findByName extends Context {
    protected override def urlSuffix: String = "boltreqs/checktest4.megam.co"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

}