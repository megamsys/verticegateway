/**
 * * Copyright [2013-2016] [Megam Systems]
 * *
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * *
 * * http://www.apache.org/licenses/LICENSE-2.0
 * *
 * * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 */

/**
 * @author rajthilak
 *
 */

package test

//import org.specs2.mutable._
import org.specs2.Specification
import java.net.URL
import org.specs2.matcher.MatchResult
import org.specs2.execute.{ Result => SpecsResult }
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import io.megam.auth.stack.HeaderConstants._
import models.base.{ SshKeysInput }

class SshKeysSpec extends Specification {
  def is =
    "SshKeysSpec".title ^ end ^
      """
      SshKeySpec is the implementation that calls the megam_play API server with the /SshKey url to create SshKeys
    """ ^ end ^
      "The Client Should" ^
    //  "Correctly do POST requests" ! Post0.succeeds ^
      //"Correctly do LIST requests with a valid userid and api key" ! List.succeeds ^
      "Correctly do GET  requests with an valid valid userid and api key" ! Get.succeeds ^
      //"Correctly do POST requests with an invalid key" ! PostInvalidUrl.succeeds ^
      //"Correctly do POST requests with an invalid body" ! PostInvalidBody.succeeds ^
      end

  //post the headers and their body for specifing url
  case object Post0 extends Context {
    protected override def urlSuffix: String = "sshkeys/content"
    protected override def bodyToStick: Option[String] = {
      val contentToEncode = new SshKeysInput("sample", "PRIVKEY0012", "PUBKEY0012").json
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
    protected override def urlSuffix: String = "sshkeys"
    protected def headersOpt: Option[Map[String, String]] = None
    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object Get extends Context {
    protected override def urlSuffix: String = "sshkeys/rr"

    protected def headersOpt: Option[Map[String, String]] = None
    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }
  case object PostInvalidUrl extends Context {
    protected override def urlSuffix: String = "sshkeys/content23"
    protected override def bodyToStick: Option[String] = {
      val contentToEncode = new SshKeysInput("sample", "PRIVKEY0012", "PUBKEY0012").json
      Some(new String(contentToEncode))
    }
    protected override def headersOpt: Option[Map[String, String]] = None

    private val post = POST(url)(httpClient)
      .addHeaders(headers)
      .addBody(body)
    def succeeds: SpecsResult = {
      val resp = execute(post)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.NotFound)
    }
  }

  case object PostInvalidBody extends Context {
    protected override def urlSuffix: String = "sshkeys/content"
    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{\"collapsedmail\":\"tee@test.com\", \"inval_api_key\":\"IamAtlas{74}NobodyCanSeeME#075488\", \"authority\":\"user\"}"
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

}
