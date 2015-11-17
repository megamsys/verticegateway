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

class AssembliesSpec extends Specification {

  def is =
    "AssembliesSpec".title ^ end ^ """
  AssembliesSpec is the implementation that calls the megam_play API server with the /assemblies url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do POST assemblies with a valid userid and api key" ! Post.succeeds  ^
      "Correctly do GET  requests with an valid Assemblies ID" ! findById.succeeds  ^
      "Correctly do LIST requests with a valid userid and api key" ! List.succeeds    ^
      end


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
        "\"inputs\":["+
        "{\"key\":\"domain\",\"value\":\"megam.co\"}," +
        "{\"key\":\"version\",\"value\":\"2.0.0\"}," +
        "{\"key\":\"varai-id\",\"value\":\"46fc26f2.b903d8\"}," +
        "{\"key\":\"varai-x\",\"value\":\"645\"}," +
        "{\"key\":\"varai-y\",\"value\":\"184\"}," +
        "{\"key\":\"varai-z\",\"value\":\"adae6e10.52519\"}," +
        "{\"key\":\"varai-wires\",\"value\":\"e0e3651f.1f1c98\"}," +
        "]," +
        "\"outputs\":[]," +
        "\"artifacts\":{" +
        "\"artifact_type\":\"tosca type\"," +
        "\"content\":\"\"," +
        "\"artifact_requirements\":[]" +
        "}," +
        "\"related_components\":[\"VernonDennis.megam.co/MasonHernandez\"]," +
        "\"operations\":[" +
        "{" +
        "\"operation_type\":\"CI\"," +
        "\"description\":\"continous Integration\"," +
        "\"operation_requirements\":[" +
        "{\"key\":\"ci-scm\",\"value\":\"github\"}," +
        "{\"key\":\"ci-enable\",\"value\":\"true\"}," +
        "{\"key\":\"ci-token\",\"value\":\"token\"}," +
        "{\"key\":\"ci-owner\",\"value\":\"owner\"}" +
        "]}]," +
        "\"status\":\"\"" +
        "}" +
        "]," +
        "\"tosca_type\":\"ubuntu\","+
        "\"requirements\":[" +
        "{\"key\":\"host\",\"value\":\"aws516887611449540608\"}," +
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
        "\"inputs\":[]," +
        "\"operations\":[]," +
        "\"outputs\":[]," +
        "\"status\":\"Launching\"" +
        "}," +
        "{" +
        "\"name\":\"VernonDennis\"," +
        "\"components\":[" +
        "{" +
        "\"name\":\"AddieOrtega\"," +
        "\"tosca_type\":\"tosca.web.java\"," +
        "\"inputs\":[" +
        "{\"key\":\"domain\",\"value\":\"megam.co\"}," +
        "{\"key\":\"source\",\"value\":\"dfhfgjh\"}," +
        "{\"key\":\"varai-id\",\"value\":\"e0e3651f.1f1c98\"}," +
        "{\"key\":\"varai-x\",\"value\":\"428\"}," +
        "{\"key\":\"varai-y\",\"value\":\"134\"}," +
        "{\"key\":\"varai-z\",\"value\":\"adae6e10.52519\"}," +
        "{\"key\":\"varai-wires\",\"value\":\"46fc26f2.b903d8\"}" +
        "]," +
        "\"outputs\":[]," +
        "\"artifacts\":{" +
        "\"artifact_type\":\"tosca type\"," +
        "\"content\":\"\"," +
        "\"artifact_requirements\":[]" +
        "}," +
        "\"related_components\":[\"PaulineHarper.megam.co/GussieMathis\"]," +
        "\"operations\":[" +
        "{" +
        "\"operation_type\":\"CI\"," +
        "\"description\":\"continous Integration\"," +
        "\"operation_requirements\":[" +
        "{\"key\":\"ci-scm\",\"value\":\"github\"}," +
        "{\"key\":\"ci-enable\",\"value\":\"true\"}," +
        "{\"key\":\"ci-token\",\"value\":\"token\"}," +
        "{\"key\":\"ci-owner\",\"value\":\"owner\"}" +
        "]}]," +
        "\"status\":\"\"" +
        "}," +
        "{" +
        "\"name\":\"MasonHernandez\"," +
        "\"tosca_type\":\"tosca.web.akka\"," +
        "\"inputs\":[" +
        "{\"key\":\"domain\",\"value\":\"megam.co\"}," +
        "{\"key\":\"source\",\"value\":\"dfghfh\"}," +
        "]," +
        "\"outputs\":[]," +
        "\"artifacts\":{" +
        "\"artifact_type\":\"tosca type\"," +
        "\"content\":\"\"," +
        "\"artifact_requirements\":[]" +
        "}," +
        "\"related_components\":[\"PaulineHarper.megam.co/GussieMathis\"]," +
        "\"operations\":[" +
        "{" +
        "\"operation_type\":\"CI\"," +
        "\"description\":\"continous Integration\"," +
        "\"operation_requirements\":[" +
        "{\"key\":\"ci-scm\",\"value\":\"github\"}," +
        "{\"key\":\"ci-enable\",\"value\":\"true\"}," +
        "{\"key\":\"ci-token\",\"value\":\"token\"}," +
        "{\"key\":\"ci-owner\",\"value\":\"owner\"}" +
        "]}]," +
        "\"status\":\"\"" +
        "}" +
        "]," +
        "\"tosca_type\":\"ubuntu\","+
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
        "\"inputs\":[]," +
        "\"operations\":[]," +
        "\"outputs\":[]," +
        "\"status\":\"Launching\"" +
        "}" +
        "]," +
        "\"inputs\":[]" +
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
*/
