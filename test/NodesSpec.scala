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
      //"Correctly do POST requests with a valid userid and api key" ! Post.succeeds ^
      //"Correctly do POST requests with an invalid body" ! PostInvalidBody.succeeds ^
     //"Correctly do GET  (emai)requests with a valid userid and api key" ! findByEmail.succeeds ^
     //"Correctly do GET  (node name)requests with an invalid Node name" ! findByName.succeeds ^
      "Correctly do GET  (check.megam.co)requests with a valid userid and api key" ! findByNameForRuby.succeeds ^
      end

  /**
   * Change the body content in method bodyToStick
   */
  case object Post extends Context {

    protected override def urlSuffix: String = "nodes/content"

    protected override def bodyToStick: Option[String] = {
      val command = new NodeCommand(new NodeSystemProvider(NodeProvider.empty),
        new NodeCompute("ec2", new NodeComputeDetail("default","106719", "100"),
          new NodeComputeAccess("megam_hp", "ubuntu", " ~/.ssh/megam_hp.pem")),
        new NodeCloudToolService(new NodeCloudToolChef("knife", "hp server create", "java", "-N todaysample.megam.co"))).json
      //val contentToEncode = "{\"node_name\":\"checktest5.megam.co\",\"node_type\":\"APP\",\"req_type\":\"CREATE\",\"command\":" +
       // command + ",\"predefs\":{\"name\":\"rails\",\"scm\":\"scm\", \"war\":\"some.war\",\"db\":\"db\", \"queue\":\"queue\"}," +
       // "\"appdefns\":{\"timetokill\":\"timetokill\",\"metered\":\"metered\", \"logging\":\"logging\",\"runtime_exec\":\"runtime_exec\"},"+
      //  "\"boltdefns\":{\"username\":\"\",\"apikey\":\"\", \"store_name\":\"\",\"url\":\"\",\"prime\":\"\",\"timetokill\":\"\",\"metered\":\"\", \"logging\":\"\",\"runtime_exec\":\"\"},\"appreq\":{},\"boltreq\":{}}"                         
     // Some(new String(contentToEncode))
      
      val contentToEncode = "{\"node_name\":\"todaysample.megam.co\",\"node_type\":\"BOLT\",\"noofinstances\":1,\"req_type\":\"CREATE\",\"command\":" +
        command + ",\"predefs\":{\"name\":\"rails\",\"scm\":\"scm\", \"war\":\"some.war\",\"db\":\"db\", \"queue\":\"queue\"}," +
        "\"appdefns\":{\"timetokill\":\"\",\"metered\":\"\", \"logging\":\"\",\"runtime_exec\":\"\"},"+
        "\"boltdefns\":{\"username\":\"rr\",\"apikey\":\"dfgythgf\", \"store_name\":\"dbname\",\"url\":\"url\",\"prime\":\"prime\",\"timetokill\":\"timetokill\",\"metered\":\"metered\", \"logging\":\"logging\",\"runtime_exec\":\"runtime_exec\"},\"appreq\":{},\"boltreq\":{}}"                         
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
      val contentToEncode = "{\"node_name\":\"atlas.megam.co\",\"command\":\"commands\",\"predefs\":{\"name\":\"rails\",\"scm\":\"scm\", \"db\":\"db\", \"queue\":\"queue\"}}"
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
    protected override def urlSuffix: String = "nodes/todaysample1.megam.co"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object findByNameForRuby extends Context {
    protected override def urlSuffix: String = "nodes/todaysample1.megam.co"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

}