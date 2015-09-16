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
import models.tosca.Domains
import test.{Context}






 * @author rajthilak
 *


class ContiniousIntegrationSpec extends Specification {
  def is =
    "ContiniousIntegrationSpec".title ^ end ^ """
      ContiniousIntegrationSpec is the implementation that calls the megam_play API server with the /cis url to create ContiniousIntegration
    """ ^ end ^
  "The Client Should" ^
  "Correctly do POST requests with a valid ContiniousIntegration name" ! Post.succeeds ^
  //"Correctly do POST requests with an invalid URL" ! PostInvalidUrl.succeeds ^
  //"Correctly do POST requests with an invalid body" ! PostInvalidBody.succeeds ^
  //"Correctly do GET requests with a valid domains name" ! Get.succeeds ^
  end


case object Post extends Context {

  protected override def urlSuffix: String = "cis/content"

  protected override def bodyToStick: Option[String] = {
    val contentToEncode = "{\"scm\":\"github\",\"component_id\":\"COM1185521959144914944\" }"
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



   * test case for invalidUrl


  case object PostInvalidUrl extends Context {

    protected override def urlSuffix: String = "cis/contentinvalidurl"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{\"scm\":\"github\",\"component_id\":\"COM1185521959144914944\" }"
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


   * test case for invalidBody


  case object PostInvalidBody extends Context {

    protected override def urlSuffix: String = "cis/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{\"name1\":\"megam.org\"}"
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



}   */
