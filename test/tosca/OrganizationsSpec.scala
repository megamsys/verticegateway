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

/**
 * @author morpheyesh
 *
 */
class OrganizationsSpec extends Specification {
  def is =
    "OrganizationsSpec".title ^ end ^
      """
      ORganizationssSpec is the implementation that calls the megam_play API server with the /MarketPlace url to create MarketPlaces
    """ ^ end ^
      "The Client Should" ^
      //"Correctly do POST requests" ! Post0.succeeds ^
      //"Correctly do POST requests" ! Post1.succeeds ^
      //"Correctly do LIST requests with a valid userid and api key" ! List.succeeds ^
      "Correctly do GET requests with a valid userid and api key" ! Get.succeeds ^
     // "Correctly do POST requests with an invalid key" ! PostInvalidUrl.succeeds ^
      //"Correctly do POST requests with an invalid body" ! PostInvalidBody.succeeds ^
     // "Correctly do GET requests with a invalid apikey" ! GetInvalidApi.succeeds ^
     // "Correctly do GET requests with a invalid email" ! GetInvalidEmail.succeeds ^
      end

*/