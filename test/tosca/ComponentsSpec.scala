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
import models.tosca.Component
import test.{ Context }
/**
 * @author rajthilak
 *
 */
class ComponentsSpec extends Specification {

  def is =
    "ComponentsSpec".title ^ end ^ """
  AssemblySpec is the implementation that calls the megam_play API server with the /assembly url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do GET  requests with an valid Assembly ID" ! findByIDApp.succeeds ^
      "Correctly do POST  requests with an valid Assembly ID" ! updateApp.succeeds ^
      end

  case object findByIDApp extends Context {
    protected override def urlSuffix: String = "components/COM1133824040297955328"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object updateApp extends Context {

    protected override def urlSuffix: String = "components/update"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
        "\"id\": \"COM1139245887592202240\"," +
        "\"name\":\"NettieMoore\"," +
        "\"tosca_type\":\"tosca.web.redis\"," +
        "\"requirements\":{" +
        "\"host\":\"clouddefault1139222212843274240\"," +
        "\"dummy\":\"\"" +
        "}," +
        "\"inputs\":{" +
        "\"domain\":\"megam.co\"," +
        "\"port\":\"6379\"," +
        "\"username\":\"\"," +
        "\"password\":\"\"," +
        "\"version\":\"\"," +
        "\"source\":\"\"," +
        "\"design_inputs\":{" +
        "\"id\":\"39bb18e7.c644e8\"," +
        "\"x\":\"802\"," +
        "\"y\":\"331\"," +
        "\"z\":\"3f43bde9.c0bc42\"," +
        "\"wires\":[\"cae50d7.f351af\"]" +
        "}," +
        "\"service_inputs\":{" +
        "\"dbname\":\"\"," +
        "\"dbpassword\":\"\"" +
        "}}," +
        "\"external_management_resource\":\"\"," +
        "\"artifacts\":{" +
        "\"artifact_type\":\"\"," +
        "\"content\":\"\"," +
        "\"artifact_requirements\":\"\"" +
        "}," +
        "\"related_components\":\"AntonioMcCormick.megam.co/TimothyHenderson\"," +
        "\"operations\":{" +
        "\"operation_type\":\"\"," +
        "\"target_resource\":\"\"" +
        "}," +
        "\"created_at\":\"2014-10-29 14:06:39 +0000\"" +
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

}