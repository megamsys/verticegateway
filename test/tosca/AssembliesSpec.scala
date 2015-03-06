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
import models.tosca.Assemblies
import test.{ Context }
/**
 * @author ram
 *
 */
class AssembliesSpec extends Specification {

  def is =
    "AssembliesSpec".title ^ end ^ """
  AssembliesSpec is the implementation that calls the megam_play API server with the /assemblies url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do POST assemblies with a valid userid and api key" ! Post.succeeds  ^
     // "Correctly do GET  requests with an valid Assemblies ID" ! findById.succeeds  ^
      //"Correctly do LIST requests with a valid userid and api key" ! List.succeeds    ^
      end

  /**
   * Change the body content in method bodyToStick
   */
  case object Post extends Context {

    protected override def urlSuffix: String = "assemblies/content"

    protected override def bodyToStick: Option[String] = {
      //  val inputs = new AssembliesInputs("9c8281e6.637d8", "tab", "Sheet 2")
      val contentToEncode = "{ \"name\":\"Sheet 1\", " +
        "\"assemblies\":[ " +
        "{ " +
        "\"name\":\"PaulineHarper\"," +
        "\"components\":[" +
        "{" +
        "\"name\":\"GussieMathis\"," +
        "\"tosca_type\":\"tosca.web.riak\"," +
        "\"requirements\":{" +
        "\"host\":\"aws516887611449540608\"," +
        "\"dummy\":\"\"" +
        "}," +
        "\"inputs\":{" +
        "\"domain\":\"megam.co\"," +
        "\"port\":\"\"," +
        "\"username\":\"\"," +
        "\"password\":\"\"," +
        "\"version\":\"2.0.0\"," +
        "\"source\":\"\"," +
        "\"design_inputs\":{" +
        "\"id\":\"46fc26f2.b903d8\"," +
        "\"x\":645," +
        "\"y\":184," +
        "\"z\":\"adae6e10.52519\"," +
        "\"wires\":[" +
        "\"e0e3651f.1f1c98\"" +
        "]" +
        "}," +
        "\"service_inputs\":{" +
        "\"dbname\":\"\"," +
        "\"dbpassword\":\"\"" +
        "}" +
        "}," +
        "\"external_management_resource\":\"\"," +
        "\"artifacts\":{" +
        "\"artifact_type\":\"tosca type\"," +
        "\"content\":\"\"," +
        "\"artifact_requirements\":\"\"" +
        "}," +
        "\"related_components\":\"VernonDennis.megam.co/MasonHernandez\"," +
        "\"operations\":{" +
        "\"operation_type\":\"\"," +
        "\"target_resource\":\"\"" +
        "}," +
        "\"others\":[{\"otherkey\":\"ci\",\"othervalue\":\"github\"}]" +
        "}" +
        "]," +
        "\"policies\":[" +
        "{" +
        "\"name\":\"bind policy\"," +
        "\"ptype\":\"colocated\"," +
        "\"members\":[" +
        "\"46fc26f2.b903d8\"" +
        "]" +
        "}" +
        "]," +
        "\"inputs\":\"\"," +
        "\"operations\":\"\"," +
        "\"status\":\"Launching\"" +
        "}," +
        "{" +
        "\"name\":\"VernonDennis\"," +
        "\"components\":[" +
        "{" +
        "\"name\":\"AddieOrtega\"," +
        "\"tosca_type\":\"tosca.web.java\"," +
        "\"requirements\":{" +
        "\"host\":\"aws516887611449540608\"," +
        "\"dummy\":\"\"" +
        "}," +
        "\"inputs\":{" +
        "\"domain\":\"megam.co\"," +
        "\"port\":\"\"," +
        "\"username\":\"\"," +
        "\"password\":\"\"," +
        "\"version\":\"\"," +
        "\"source\":\"dfhfgjh\"," +
        "\"design_inputs\":{" +
        "\"id\":\"e0e3651f.1f1c98\"," +
        "\"x\":428," +
        "\"y\":134," +
        "\"z\":\"adae6e10.52519\"," +
        "\"wires\":[" +
        "\"46fc26f2.b903d8\"" +
        "]" +
        "}," +
        "\"service_inputs\":{" +
        "\"dbname\":\"\"," +
        "\"dbpassword\":\"\"" +
        "}" +
        "}," +
        "\"external_management_resource\":\"\"," +
        "\"artifacts\":{" +
        "\"artifact_type\":\"tosca type\"," +
        "\"content\":\"\"," +
        "\"artifact_requirements\":\"\"" +
        "}," +
        "\"related_components\":\"PaulineHarper.megam.co/GussieMathis\"," +
        "\"operations\":{" +
        "\"operation_type\":\"\"," +
        "\"target_resource\":\"\"" +
        "}," +
        "\"others\":[{\"otherkey\":\"ci\",\"othervalue\":\"github\"}]," +
        "}," +
        "{" +
        "\"name\":\"MasonHernandez\"," +
        "\"tosca_type\":\"tosca.web.akka\"," +
        "\"requirements\":{" +
        "\"host\":\"aws516887611449540608\"," +
        "\"dummy\":\"\"" +
        "}," +
        "\"inputs\":{" +
        "\"domain\":\"megam.co\"," +
        "\"port\":\"\"," +
        "\"username\":\"\"," +
        "\"password\":\"\"," +
        "\"version\":\"\"," +
        "\"source\":\"dfghfh\"," +
        "\"design_inputs\":{" +
        "\"id\":\"3ecdffaf.c132\"," +
        "\"x\":450," +
        "\"y\":297," +
        "\"z\":\"adae6e10.52519\"," +
        "\"wires\":[" +
        "\"46fc26f2.b903d8\"" +
        "]" +
        "}," +
        "\"service_inputs\":{" +
        "\"dbname\":\"\"," +
        "\"dbpassword\":\"\"" +
        "}" +
        "}," +
        "\"external_management_resource\":\"\"," +
        "\"artifacts\":{" +
        "\"artifact_type\":\"tosca type\"," +
        "\"content\":\"\"," +
        "\"artifact_requirements\":\"\"" +
        "}," +
        "\"related_components\":\"PaulineHarper.megam.co/GussieMathis\"," +
        "\"operations\":{" +
        "\"operation_type\":\"\"," +
        "\"target_resource\":\"\"" +
        "}," +
        "\"others\":[{\"otherkey\":\"ci\",\"othervalue\":\"github\"}]," +
        "}" +
        "]," +
        "\"policies\":[" +
        "{" +
        "\"name\":\"bind policy\"," +
        "\"ptype\":\"colocated\"," +
        "\"members\":[" +
        "\"e0e3651f.1f1c98\"," +
        "\"3ecdffaf.c132\"" +
        "]" +
        "}" +
        "]," +
        "\"inputs\":\"\"," +
        "\"operations\":\"\"," +
        "\"status\":\"Launching\"" +
        "}" +
        "]," +
        "\"inputs\":{" +
        "\"id\":\"adae6e10.52519\"," +
        "\"assemblies_type\":\"tab\"," +
        "\"label\":\"Sheet 1\"," +
        "\"cloudsettings\":[" +
        "{" +
        "\"id\":\"53ef867c.ac1078\"," +
        "\"cstype\":\"cloudsettings\"," +
        "\"cloudsettings\":\"aws516887611449540608\"," +
        "\"x\":186," +
        "\"y\":215," +
        "\"z\":\"adae6e10.52519\"," +
        "\"wires\":[" +
        "\"e0e3651f.1f1c98\"," +
        "\"3ecdffaf.c132\"," +
        "\"46fc26f2.b903d8\"" +
        "]" +
        "}" +
        "]" +
        "}" +
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
    protected override def urlSuffix: String = "assemblies"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object findById extends Context {
    protected override def urlSuffix: String = "assemblies/AMS520198742872162304"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

}