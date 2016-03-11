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

package test

import org.specs2.mutable._
import org.specs2.Specification
import java.net.URL
import org.specs2.matcher.MatchResult
import org.specs2.execute.{ Result => SpecsResult }
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import models.base._
import test._

class RequestSpec extends Specification {

  def is =
    "RequestSpec".title ^ end ^ """
  RequestSpec is the implementation that calls the API server with the /requests url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do POST requests for a torpedo(create)" ! TorpedoCreate.succeeds ^
      "Correctly do POST requests for a torpedo(start)" ! TorpedoStart.succeeds ^
      "Correctly do POST requests for a torpedo(stop)" ! TorpedoStop.succeeds ^
      "Correctly do POST requests for a torpedo(delete)" ! TorpedoDelete.succeeds ^
      "Correctly do POST requests for a app(create)" ! AppCreate.succeeds ^
      "Correctly do POST requests for a app(start)" ! AppStart.succeeds ^
      "Correctly do POST requests for a app(stop)" ! AppStart.succeeds ^
      "Correctly do POST requests for a app(delete)" ! AppDelete.succeeds ^
      "Correctly do POST requests for a app(restart)" ! AppRestart.succeeds ^
      "Correctly do POST requests for a service(create)" ! ServiceCreate.succeeds ^
      "Correctly do POST requests for a service(start)" ! ServiceStart.succeeds ^
      "Correctly do POST requests for a service(stop)" ! ServiceStop.succeeds ^
      "Correctly do POST requests for a service(delete)" ! ServiceDelete.succeeds ^
      "Correctly do POST requests for a service(restart)" ! ServiceRestart.succeeds ^
      "Correctly do POST requests for a docker(create)" ! DockerCreate.succeeds ^
      "Correctly do POST requests for a docker(start)" ! DockerStart.succeeds ^
      "Correctly do POST requests for a docker(delete)" ! DockerDelete.succeeds ^
  end

  case object TorpedoCreate extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.torpedo.ubuntu", "", "create", "state").json
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

  case object TorpedoStart extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.torpedo.ubuntu",
        "katru.megambox.com", "start", "CONTROL").json
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

  case object TorpedoStop extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.torpedo.ubuntu",
        "katru.megambox.com", "stop", "CONTROL").json
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

  case object TorpedoDelete extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.torpedo.ubuntu",
        "katru.megambox.com", "destroy", "state").json
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

  //app

  case object AppCreate extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.app.java",
        "", "create", "state").json
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

  case object AppStart extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.app.java",
        "katru.megambox.com", "start", "CONTROL").json
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

  case object AppStop extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.app.java",
        "katru.megambox.com", "stop", "CONTROL").json
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
  
  case object AppRestart extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.app.java",
        "katru.megambox.com", "restart", "CONTROL").json
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

  case object AppDelete extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.app.java",
        "katru.megambox.com", "destroy", "state").json
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

  //service

  case object ServiceCreate extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.service.postgresql",
        "", "create", "state").json
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

  case object ServiceStart extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.service.postgresql",
        "katru.megambox.com", "start", "CONTROL").json
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

  case object ServiceStop extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.service.postgresql",
        "katru.megambox.com", "stop", "CONTROL").json
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

  case object ServiceDelete extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.service.postgresql",
        "katru.megambox.com", "destroy", "state").json
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
  
  case object ServiceRestart extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.service.postgresql",
        "katru.megambox.com", "restart", "CONTROL").json
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

  //docker

  case object DockerCreate extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.microservice.dockercontainer",
        "", "create", "state").json
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

  case object DockerStart extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.microservice.dockercontainer",
        "katru.megambox.com", "start", "CONTROL").json
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

  case object DockerStop extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.microservice.dockercontainer",
        "katru.megambox.com", "stop", "CONTROL").json
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

  case object DockerDelete extends Context {

    protected override def urlSuffix: String = "requests/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = RequestInput("ASM1136003656177549312", "tosca.microservice.dockercontainer",
        "katru.megambox.com", "destroy", "state").json
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
