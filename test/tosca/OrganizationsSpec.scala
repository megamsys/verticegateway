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
* */

package test.tosca

import org.specs2.mutable._
import org.specs2.Specification
import java.net.URL
import org.specs2.matcher.MatchResult
import org.specs2.execute.{ Result => SpecsResult }
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import models.tosca._
import models.tosca.Organizations
import test.{ Context }

/**
 * @author morpheyesh
 *
 */

class OrganizationsSpec extends Specification {
  def is =
    "OrganizationsSpec".title ^ end ^ """
      OrganizationssSpec is the implementation that calls the megam_play API server with the /MarketPlace url to create MarketPlaces
    """ ^ end ^
      "The Client Should" ^
      //"Correctly do POST requests with a valid organizations name" ! Post.succeeds ^
      //"Correctly do POST requests with an invalid URL" ! PostInvalidUrl.succeeds ^
      //"Correctly do POST requests with an invalid body" ! PostInvalidBody.succeeds ^
     // "Correctly do GET requests with a valid organizations name" ! Get.succeeds ^
        "Correctly do LIST request with a valid email id" ! List.succeeds ^
      end

  /**
   * Change the body content in method bodyToStick
   */

  case object Post extends Context {

    protected override def urlSuffix: String = "organizations/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{\"name\":\"Megam6\" }"
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
  //Success

  /**
   * test case for invalidUrl
   */

  case object PostInvalidUrl extends Context {

    protected override def urlSuffix: String = "organizations/contentinvalidurl"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{\"name\":\"FAKEORG\"}"
      Some(new String(contentToEncode))
    }
    protected override def headersOpt: Option[Map[String, String]] = None

    private val post = POST(url)(httpClient)
      .addHeaders(headers)
      .addBody(body)

    def succeeds: SpecsResult = {
      val resp = execute(post)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.NotFound)
    }

  }
  //Success

  /**
   * test case for invalidBody
   */

  case object PostInvalidBody extends Context {

    protected override def urlSuffix: String = "organizations/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{\"name1\":\"FAKEORG\"}"
      Some(new String(contentToEncode))
    }
    protected override def headersOpt: Option[Map[String, String]] = None

    private val post = POST(url)(httpClient)
      .addHeaders(headers)
      .addBody(body)

    def succeeds: SpecsResult = {
      val resp = execute(post)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.ServiceUnavailable)
    }
  }

  /*
   * Testing 'GET' request.
   * 
   * 
   */

  case object Get extends Context {
    protected override def urlSuffix: String = "organizations/Megam"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }
  //Success
  
  case object List extends Context {
    protected override def urlSuffix: String = "organizations"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }


}
