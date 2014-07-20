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
import org.specs2.matcher.MatchResult
import org.specs2.execute.{ Result => SpecsResult }
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import controllers.stack.SecurityActions._
import models.{ CloudToolSettingInput }

class CloudToolSettingSpec extends Specification {
  def is =
    "CloudToolSettingSpec".title ^ end ^
      """
      CloudToolSettingSpec is the implementation that calls the megam_play API server with the /CloudToolSetting url to create CloudToolSettings
    """ ^ end ^
      "The Client Should" ^
      "Correctly do POST requests" ! Post0.succeeds ^
      //"Correctly do POST requests" ! Post1.succeeds ^
     // "Correctly do LIST requests with a valid userid and api key" ! List.succeeds ^
      //"Correctly do GET requests with a valid userid and api key" ! Get.succeeds ^
      // "Correctly do POST requests with an invalid key" ! PostInvalidUrl.succeeds ^
      //"Correctly do POST requests with an invalid body" ! PostInvalidBody.succeeds ^
      // "Correctly do GET requests with a invalid apikey" ! GetInvalidApi.succeeds ^
      // "Correctly do GET requests with a invalid email" ! GetInvalidEmail.succeeds ^
      end

  //post the headers and their body for specifing url
  case object Post0 extends Context {

    protected override def urlSuffix: String = "cloudtoolsettings/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = new CloudToolSettingInput("chef", "default_chef", "https://github.com/indykish/chef-repo.git", "https://s3-ap-southeast-1.amazonaws.com/cloudrecipes/sandy@megamsandbox.com/default_chef/chef-repo.zip", "cloudrecipes/sandy@megamsandbox.com/default_chef/chef-repo/.chef/knife.rb").json
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

    protected override def urlSuffix: String = "cloudtoolsettings/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = new CloudToolSettingInput("chef", "test", "https://github.com", "https://s3-ap-southeast-1.amazonaws.com/cloudrecipes/sandy@megamsandbox.com/default", "sandy@megamsandbox.com/default").json
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
    protected override def urlSuffix: String = "cloudtoolsettings"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object Get extends Context {
    protected override def urlSuffix: String = "cloudtoolsettings/CTS410306550977003520"

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

    protected override def urlSuffix: String = "cloudtoolsettings/contentinvalidurl"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = new CloudToolSettingInput("chef", "test", "https://github.com", "https://s3-ap-southeast-1.amazonaws.com/cloudkeys/sandy@megamsandbox.com/default", "sandy@megamsandbox.com/default").json
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

    protected override def urlSuffix: String = "cloudtoolsettings/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = new CloudToolSettingInput("chef", "test", "https://github.com", "https://s3-ap-southeast-1.amazonaws.com/cloudkeys/sandy@megamsandbox.com/default", "sandy@megamsandbox.com/default").json
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
    protected override def urlSuffix: String = "cloudtoolsettings/ec2_rails"

    protected override def headersOpt: Option[Map[String, String]] = Some(Map(Content_Type -> application_json,
      X_Megam_EMAIL -> "sandy@megamsandbox.com", X_Megam_APIKEY -> "i@a)23_mC-han^00g57#ed8a+p%i",
      X_Megam_DATE -> currentDate, Accept -> application_vnd_megam_json))

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Unauthorized)
    }
  }

  case object GetInvalidEmail extends Context {
    protected override def urlSuffix: String = "cloudtoolsettings/meg-rails"

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
  