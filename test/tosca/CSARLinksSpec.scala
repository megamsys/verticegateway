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
/**
 * @author rajthilak
 *
 */
package test.tosca

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
import models.tosca.{ CSARInput }
import test.{Context}

class CSARLinksSpec extends Specification {
  def is =
    "CSARLinksSpec".title ^ end ^
      """
      CSARLinksSpec is the implementation that calls the megam_play API server with the /csars url to create csars
    """ ^ end ^
      "The Client Should" ^
     "Correctly do GET requests with a valid userid and api key" ! Get.succeeds ^
      end
 
 
  case object Get extends Context {
    protected override def urlSuffix: String = "csars/CSI1147542768063741952"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  } 

}
  