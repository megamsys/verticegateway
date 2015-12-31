/*
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
package test.billing

import org.specs2.mutable._
import org.specs2.Specification
import java.net.URL
import org.specs2.matcher.MatchResult
import org.specs2.execute.{ Result => SpecsResult }
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import models.json.billing._
import test.{ Context }

 class InvoicesSpec extends Specification {

  def is =
    "InvoicesSpec".title ^ end ^ """
InvoicesSpec is the implementation that calls the megam_play API server with the /invoices url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do POST  requests with an valid datas "! create.succeeds^
      "Correctly do LIST requests with a valid userid and api key" ! List.succeeds ^
      "Correctly do GET requests with a valid userid and api key" ! Get.succeeds ^
      end

    case object create extends Context {

    protected override def urlSuffix: String = "invoices/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
        "\"from_date\": \"2015-10-07\"," +
        "\"to_date\":\"2015-11-07\"," +
        "\"month\":\"oct\"," +
        "\"bill_type\": \"paypal\"," +
        "\"billing_amount\":\"2000\"," +
        "\"currency_type\":\"USD\"," +
        "}"

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
  case object List extends Context {
    protected override def urlSuffix: String = "invoices"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }
  case object Get extends Context {
    protected override def urlSuffix: String = "invoices/INV1280838174226120704"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

}
