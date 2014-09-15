/* 
** Copyright [2013-2014] [Megam Systems]
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
import controllers.Constants._
import org.specs2.matcher.MatchResult
import org.specs2.execute.{ Result => SpecsResult }
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import controllers.stack.HeaderConstants._
import models.{ PredefCloudInput, PredefCloudSpec, PredefCloudAccess }

class PredefCloudsSpec extends Specification {
  def is =
    "PredefsCloudSpec".title ^ end ^
      """
      PredefCloudsSpec is the implementation that calls the megam_play API server with the /predefcloud url to create predefclouds
    """ ^ end ^
      "The Client Should" ^
     // "Correctly do POST requests" ! Post0.succeeds ^
      //"Correctly do POST requests" ! Post1.succeeds ^
      "Correctly do LIST requests with a valid userid and api key" ! List.succeeds ^
      //"Correctly do GET requests with a valid userid and api key" ! Get.succeeds ^
     // "Correctly do POST requests with an invalid key" ! PostInvalidUrl.succeeds ^
      //"Correctly do POST requests with an invalid body" ! PostInvalidBody.succeeds ^
     // "Correctly do GET requests with a invalid apikey" ! GetInvalidApi.succeeds ^
     // "Correctly do GET requests with a invalid email" ! GetInvalidEmail.succeeds ^
      end

  //post the headers and their body for specifing url
  case object Post0 extends Context {

    protected override def urlSuffix: String = "predefclouds/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = new PredefCloudInput("clouddefault",
    new PredefCloudSpec("google", "", "debian-7-wheezy-v20131120", "f1-micro", ""),
    new PredefCloudAccess("", "cloudkeys/" + MEGAM_ADMIN_EMAIL + "/id_rsa.pub", "ubuntu", "https://s3-ap-southeast-1.amazonaws.com/cloudkeys/" + MEGAM_ADMIN_EMAIL + "/gdefault", "", "europe-west1-a", "")).json
      Some(contentToEncode)
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

  //post the headers and their body for specifing url (insert one more record)
  case object Post1 extends Context {

    protected override def urlSuffix: String = "predefclouds/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = new PredefCloudInput("ec2_play", new PredefCloudSpec("fooz-type", "fooz-group", "fooz-image", "fooz-flvr", ""),
        new PredefCloudAccess("fooz-ssh", "fooz-identity-file", "fooz-sshuser", "https://s3-ap-southeast-1.amazonaws.com/cloudkeys/megam@mypaas.io/default", "fooz-identity-file", "", "region")).json
      Some(contentToEncode)
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

  case object List extends Context {
    protected override def urlSuffix: String = "predefclouds"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object Get extends Context {
    protected override def urlSuffix: String = "predefclouds/ec2_rails"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  /**
   * test case for invalidUrl
   */

  case object PostInvalidUrl extends Context {

    protected override def urlSuffix: String = "predefclouds/contentinvalidurl"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{\"email\":\"megam@mypaas.io\", \"api_key\":\"IamAtlas{74}NobodyCanSeeME#075488\", \"authority\":\"user\" }"
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

  /**
   * test case for invalidBody
   */

  case object PostInvalidBody extends Context {

    protected override def urlSuffix: String = "predefclouds/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{\"collapsedmail\":\"megam@mypaas.io\", \"inval_api_key\":\"IamAtlas{74}NobodyCanSeeME#075488\", \"authority\":\"user\" }"
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

  case object GetInvalidApi extends Context {
    protected override def urlSuffix: String = "predefclouds/ec2_rails"

    protected override def headersOpt: Option[Map[String, String]] = Some(Map(Content_Type -> application_json,
      X_Megam_EMAIL -> "megam@mypaas.io", X_Megam_APIKEY -> "i@a)23_mC-han^00g57#ed8a+p%i",
      X_Megam_DATE -> currentDate, Accept -> application_vnd_megam_json))

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Unauthorized)
    }
  }

  case object GetInvalidEmail extends Context {
    protected override def urlSuffix: String = "predefcouds/meg-rails"

    protected override def headersOpt: Option[Map[String, String]] = Some(Map(Content_Type -> application_json,
      X_Megam_EMAIL -> "sandy@bogusandbox.com", X_Megam_APIKEY -> "IamAtlas{74}NobodyCanSeeME#07",
      X_Megam_DATE -> currentDate, Accept -> application_vnd_megam_json))

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.NotFound)
    }
  }

}
  