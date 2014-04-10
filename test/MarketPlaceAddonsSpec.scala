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
/**
 * @author ram
 *
 */
class MarketPlaceAddonsSpec extends Specification {

  def is =
    "MarketPlaceAddonsSpec".title ^ end ^ """
  MarketPlaceAddonsSpec is the implementation that calls the megam_play API server with the /requests url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do POST appdefns with a valid userid and api key" ! Post.succeeds ^
      //"Correctly do GET  (node name)MarketPlaceAddons with a invalid Node name" ! findByInvalidName.succeeds ^
      //"Correctly do GET  (addon id)MarketPlaceAddons with a valid Addon id" ! findByAddonId.succeeds ^
      //"Correctly do GET  (node name)MarketPlaceAddons with a valid appdefn name" ! findByDefnsId.succeeds ^
  end

  /**
   * Change the body content in method bodyToStick
   */
  case object Post extends Context {

    protected override def urlSuffix: String = "marketplaceaddons/content"

    protected override def bodyToStick: Option[String] = {
      val config = new MarketPlaceAddonsConfig(new MarketPlaceAddonsConfigDisaster("locations", "sample1.megam.co", "appsample2.megam.co;appsample3.megam.co", "recipe[megam_drbd]"), new MarketPlaceAddonsConfigLoadBalancing("", "", ""),
                                               new MarketPlaceAddonsConfigAutoScaling("", "", "", ""), new MarketPlaceAddonsConfigMonitoring("op5", ""))
      val contentToEncode = "{\"node_id\":\"NOD451721484906266624\",\"node_name\":\"lobotomies1.megam.co\",\"marketplace_id\":\"tyutyujyt\",\"config\":" + config.json + "}"
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
    protected override def urlSuffix: String = "appdefns/checksample"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.NotFound)
    }
  }

  case object findByAddonId extends Context {
    protected override def urlSuffix: String = "marketplaceaddons/appsample1.megam.co"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object findByDefnsId extends Context {
    protected override def urlSuffix: String = "appdefns/asphyxiated1.megam.co/ADF412857952341327872"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

}