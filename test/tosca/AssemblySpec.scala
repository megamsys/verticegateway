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
import models.tosca.Assembly
import test.{ Context }
/**
 * @author rajthilak
 *
 */
class AssemblySpec extends Specification {

  def is =
    "AssemblySpec".title ^ end ^ """
  AssemblySpec is the implementation that calls the megam_play API server with the /assembly url
  """ ^ end ^
      "The Client Should" ^
      // "Correctly do GET  requests with an valid Assembly ID" ! findByIDApp.succeeds ^
      "Correctly do POST requests with an valid Assembly ID" ! updateApp.succeeds ^
      end

  case object findByIDApp extends Context {
    protected override def urlSuffix: String = "assembly/ASM1136003656177549312"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object updateApp extends Context {

    protected override def urlSuffix: String = "assembly/update"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
        "\"id\":\"ASM1139235178976247808\"," +
        "\"json_claz\":\"Megam::Assembly\"," +
        "\"name\":\"calcines\"," +
        "\"components\":[\"COM1139235178934304768\",\"\"]," +
        "\"policies\":[{ " +
        "\"name\":\"bind policy\"," +
        "\"ptype\":\"colocated\"," +
        "\"members\":[\"calcines.megam.co/MattieGarcia\",\"calcines.megam.co/parsnip\"]" +
        "}]," +
        "\"inputs\":\"\"," +
        "\"operations\":\"\"," +
        "\"created_at\":\"2014-10-29 13:24:06 +0000\"" +
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