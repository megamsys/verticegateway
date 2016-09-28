/**
 * * Copyright [2013-2016] [Megam Systems]
 * *
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * *
 * * http://www.apache.org/licenses/LICENSE-2.0
 * *
 * * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
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
import org.apache.http.impl.execchain.ClientExecChain
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import scala.concurrent._
import scala.concurrent.duration._
import java.net.URL
import io.megam.auth.stack.HeaderConstants._
import models.base._
import test._

class MarketPlaceSpec extends Specification {
  def is =
    "MarketPlacesSpec".title ^ end ^
      """
      MarketPlacesSpec is the implementation that calls the API server with the /marketplaces url to create MarketPlaces
    """ ^ end ^
      "The Client Should" ^
      //"Correctly do LIST requests with a valid userid and api key" ! List.succeeds ^
      "Correctly do GET requests with a valid userid and api key" ! Get.succeeds ^
      //"Correctly do GET requests with a invalid apikey" ! GetInvalidApi.succeeds ^
      //"Correctly do GET requests with a invalid email" ! GetInvalidEmail.succeeds ^
      end

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
    protected override def urlSuffix: String = "marketplaces/Ubuntu"

    protected def headersOpt: Option[Map[String, String]] = None
    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object GetInvalidApi extends Context {
    protected override def urlSuffix: String = "marketplaces/ec2_rails"

    protected override def headersOpt: Option[Map[String, String]] = Some(Map(Content_Type -> application_json,
      X_Megam_EMAIL -> "test@megam.io", X_Megam_APIKEY -> "i@a)23_mC-han^00g57#ed8a+p%i",
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
