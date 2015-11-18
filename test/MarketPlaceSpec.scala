/**
 ** Copyright [2013-2015] [Megam Systems]
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

import scalaz._
import scalaz.syntax.SemigroupOps
import scalaz.NonEmptyList._
import scalaz.Validation._
import org.specs2.mutable._
import org.specs2.Specification
import java.net.URL
import org.specs2.matcher.MatchResult
import org.specs2.execute.{ Result => SpecsResult }
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import controllers.stack.HeaderConstants._
import models.{ MarketPlaceInput, KeyValueList, MarketPlacePlans }

class MarketPlaceSpec extends Specification {
  def is =
    "MarketPlacesSpec".title ^ end ^
      """
      MarketPlacesSpec is the implementation that calls the megam_play API server with the /MarketPlace url to create MarketPlaces
    """ ^ end ^
      "The Client Should" ^
      "Correctly do POST requests" ! Post0.succeeds ^
      "Correctly do POST requests" ! Post1.succeeds ^
      "Correctly do LIST requests with a valid userid and api key" ! List.succeeds ^
      "Correctly do GET requests with a valid userid and api key" ! Get.succeeds ^
      "Correctly do POST requests with an invalid key" ! PostInvalidUrl.succeeds ^
      "Correctly do POST requests with an invalid body" ! PostInvalidBody.succeeds ^
      "Correctly do GET requests with a invalid apikey" ! GetInvalidApi.succeeds ^
      "Correctly do GET requests with a invalid email" ! GetInvalidEmail.succeeds ^
      end

  //post the headers and their body for specifing url

  case object Post0 extends Context {

    protected override def urlSuffix: String = "marketplaces/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = new MarketPlaceInput("test-Alfresco",
        new MarketPlaceCatalog("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/alfresco.png",
            "ECM",
            "Alfresco Community Edition allows organizations to manage any type of content from simple office documents to scanned images, photographs, engineering drawings and large video files. It is commonly used as a: Document management system, Content platform, CMIS-compliant repository"),
        new MarketPlaceFeatures("Many companies have documents stored all over the place – on desktop computers, laptops, network drives, email, USB sticks and various consumer file sharing sites. And with contracts stored by customer, invoices by month, case files by case number, and consulting reports by year, it is difficult to get a 360° view of a customer’s information, which makes effective collaboration almost impossible.",
            "Alfresco enables you to manage your business critical documents like contracts, proposals, agreements, marketing and sales materials, as well as technical renderings and manuals","Add-Ons — Ability to download and install additional product extensions (see http://addons.alfresco.com/)", "Alfresco saves valuable time otherwise wasted searching for information and recreating misplaced documents, and eliminates mistakes and costs associated with using the wrong version."),
        MarketPlacePlans(List((new MarketPlacePlan("0", "Alfresco community edition", "free","4.2", "Work in progress.")), ((new MarketPlacePlan("0", "Alfresco community edition", "free","4.2", "Work in progress."))))),
            new MarketPlaceAppLinks("", "", "", "", "", "", ""), "false", "predefnode", "false").json
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

    protected override def urlSuffix: String = "marketplaces/content"

    protected override def bodyToStick: Option[String] = {
          val contentToEncode = new MarketPlaceInput("test-Zarafa", new MarketPlaceCatalog("https://s3-ap-southeast-1.amazonaws.com/megampub/images/market_place_images/zarafa.png", "Email", ""), new MarketPlaceFeatures("", "", "", ""), MarketPlacePlans(List(new MarketPlacePlan("0", "", "free","",""))), new MarketPlaceAppLinks("#", "#", "#", "#", "#", "#", "#"), "false", "predefnode", "false").json
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
    protected override def urlSuffix: String = "marketplaces"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object Get extends Context {
    protected override def urlSuffix: String = "marketplaces/34-Trac"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }




  case object PostInvalidUrl extends Context {

    protected override def urlSuffix: String = "marketplaces/contentinvalidurl"

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





  case object PostInvalidBody extends Context {

    protected override def urlSuffix: String = "marketplaces/content"

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
    protected override def urlSuffix: String = "marketplaces/ec2_rails"

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
    protected override def urlSuffix: String = "marketplaces/meg-drbd"

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
