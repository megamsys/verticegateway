/* Copyright [2012-2013] [Megam Systems]
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
 * @author subash
 *
 */
class NodesSpec extends Specification {

  def is =
    "NodesSpec".title ^ end ^ """
  NodesSpec is the implementation that calls the megam_play API server with the /nodes url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do POST requests with a valid userid and api key" ! Post.succeeds ^
     //"Correctly do POST requests with an invalid body" ! PostInvalidBody.succeeds ^
      //"Correctly do GET  (emai)requests with a valid userid and api key" ! findByEmail.succeeds ^
      //"Correctly do GET  (node name)requests with a valid userid and api key" ! findByName.succeeds ^
      //"Correctly do GET  (morning.megam.co)requests with a valid userid and api key" ! findByNameForRuby.succeeds ^
      end

  /**
   * Change the body content in method bodyToStick
   */
  case object Post extends Context {

    protected override def urlSuffix: String = "nodes/content"

    protected override def bodyToStick: Option[String] = {
      val command = new NodeCommand(new NodeSystemProvider(NodeProvider.empty),
        new NodeCompute("ec2", new NodeComputeDetail("img1", "t1-micro"),
          new NodeComputeAccess("megam_ec2", "ubuntu", "~/sss.pem")),
        new NodeCloudToolService(new NodeCloudToolChef("knife", "ec2 server create", "java", "-N someone.megam.co"))).json
      val contentToEncode = "{\"node_name\":\"atlas.megam.co\",\"command\":" +
        command + ",\"predefs\":{\"name\":\"rails\",\"scm\":\"scm\", \"war\":\"some.war\",\"db\":\"db\", \"queue\":\"queue\"}}"
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

  case object PostInvalidBody extends Context {

    protected override def urlSuffix: String = "nodes/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{\"nodeo_name\":\"atlas.megam.co\",\"command\":\"commands\",\"predefs\":{\"name\":\"rails\",\"scm\":\"scm\", \"db\":\"db\", \"queue\":\"queue\"}}"
      Some(new String(contentToEncode))
    }
    protected override def headersOpt: Option[Map[String, String]] = None

    private val post = POST(url)(httpClient)
      .addHeaders(headers)
      .addBody(body)

    def succeeds: SpecsResult = {
      val resp = execute(post)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.BadRequest)
    }
  }

  case object findByEmail extends Context {
    protected override def urlSuffix: String = "nodes"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }
  case object findByName extends Context {
    protected override def urlSuffix: String = "nodes/atlas.megam.co"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object findByNameForRuby extends Context {
    protected override def urlSuffix: String = "nodes/morning.megam.co"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

}