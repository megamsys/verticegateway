/*
** Copyright [2013-2016] [Megam Systems]
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
import models.tosca._
import io.megam.auth.stack.HeaderConstants._
import scala.concurrent._
import scala.concurrent.duration._
import models.tosca.Assemblies
import test.{ Context }
import test._

class AssembliesSpec extends Specification {

  def is =
    "AssembliesSpec".title ^ end ^ """
  AssembliesSpec is the implementation that calls the megam_play API server with the /assemblies url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do POST assemblies with a valid userid and api key" ! Post.succeeds ^
      "Correctly do GET  requests with an valid Assemblies ID" ! idNotFound.succeeds ^
      "Correctly do LIST requests with a valid userid and api key" ! List.succeeds ^
      end

  case object Post extends Context {

    protected override def urlSuffix: String = "assemblies/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{\"name\":\"\", \"org_id\":\"ORG123\",\"assemblies\":[{\"name\":\"covey\",\"json_claz\":\"Megam::Assembly\",\"tosca_type\":\"tosca.app.java\",\"inputs\":[{\"key\":\"domain\",\"value\":\"megambox.com\"},{\"key\":\"sshkey\",\"value\":\"a@b.com_rtr\"},{\"key\":\"provider\",\"value\":\"one\"},{\"key\":\"cpu\",\"value\":\"0.5\"},{\"key\":\"ram\",\"value\":\"896\"},{\"key\":\"version\",\"value\":\"8.x\"},{\"key\":\"lastsuccessstatusupdate\",\"value\":\"02 Feb 16 13:20 IST\"},{\"key\":\"status\",\"value\":\"error\"}],\"outputs\":[],\"policies\":[],\"status\":\"error\",\"state\":\"error\",\"created_at\":\"2016-02-02 07:50:49 +0000\",\"components\":[{\"name\":\"sheba\",\"tosca_type\":\"tosca.app.java\",\"inputs\":[{\"key\":\"domain\",\"value\":\"megambox.com\"},{\"key\":\"sshkey\",\"value\":\"a@b.com_rtr\"},{\"key\":\"provider\",\"value\":\"one\"},{\"key\":\"cpu\",\"value\":\"0.5\"},{\"key\":\"ram\",\"value\":\"896\"},{\"key\":\"version\",\"value\":\"8.x\"},{\"key\":\"lastsuccessstatusupdate\",\"value\":\"02 Feb 16 13:20 IST\"},{\"key\":\"status\",\"value\":\"error\"}],\"outputs\":[],\"envs\":[{\"key\":\"port\",\"value\":\"8080\"},{\"key\":\"tomcat_username\",\"value\":\"megam\"},{\"key\":\"tomcat_password\",\"value\":\"megam\"}],\"repo\":{\"rtype\":\"source\",\"source\":\"github\",\"oneclick\":\"\",\"url\":\"https://github.com/rajthilakmca/java-spring-petclinic.git\"},\"artifacts\":{\"artifact_type\":\"\",\"content\":\"\",\"requirements\":[]},\"related_components\":[],\"operations\":[{\"operation_type\":\"CI\",\"description\":\"always up to date code. sweet.\",\"properties\":[{\"key\":\"type\",\"value\":\"github\"},{\"key\":\"token\",\"value\":\"066b697558f048459412410483ca8965415bf7de\"},{\"key\":\"username\",\"value\":\"rajthilakmca\"}],\"status\":\"notbound\"}],\"status\":\"error\",\"state\":\"error\"}]}],\"inputs\":[{\"key\":\"domain\",\"value\":\"megambox.com\"},{\"key\":\"sshkey\",\"value\":\"a@b.com_rtr\"},{\"key\":\"provider\",\"value\":\"one\"},{\"key\":\"cpu\",\"value\":\"0.5\"},{\"key\":\"ram\",\"value\":\"896\"},{\"key\":\"version\",\"value\":\"8.x\"},{\"key\":\"lastsuccessstatusupdate\",\"value\":\"02 Feb 16 13:20 IST\"},{\"key\":\"status\",\"value\":\"error\"}]}"
      Some(contentToEncode)
    }

    protected override def headersOpt: Option[Map[String, String]] = None

    private val post = POST(url)(httpClient)
      .addHeaders(headers)
      .addBody(body)

    def succeeds: SpecsResult = {
      val resp = execute(post)
      print(resp)
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

  case object idNotFound extends Context {
    protected override def urlSuffix: String = "assemblies/AMS4910703635659237712"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.NotFound)
    }
  }

}
