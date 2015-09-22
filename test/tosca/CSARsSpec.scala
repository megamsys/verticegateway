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

 * @author rajthilak
 *

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
import models.tosca.{ CSARInput }
import test.{Context}

class CSARsSpec extends Specification {
  def is =
    "CSARsSpec".title ^ end ^
      """
      CSARsSpec is the implementation that calls the megam_play API server with the /csars url to create csars
    """ ^ end ^
      "The Client Should" ^
    // "Correctly do POST requests" ! Post.succeeds ^
      "Correctly do PUSH requests" ! Push.succeeds ^
    // "Correctly do LIST requests with a valid userid and api key" ! List.succeeds ^
   //  "Correctly do GET requests with a valid userid and api key" ! Get.succeeds ^
   //  "Correctly do POST requests with an invalid body" ! PostInvalidBody.succeeds ^
      end

  //post the headers and their body for specifing url
  case object Post extends Context {

    protected override def urlSuffix: String = "csars/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = scala.io.Source.fromFile("./test/tosca/appgroup.csar").mkString
      Some(contentToEncode)
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

  case object Push extends Context {

    protected override def urlSuffix: String = "csars/push/CSR1217821599709921280"

   protected override def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds: SpecsResult = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Created)
    }

  }

  case object List extends Context {
    protected override def urlSuffix: String = "csars"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object Get extends Context {
    protected override def urlSuffix: String = "csars/CSI1147543125976285184"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }


   * test case for invalidBody

  case object PostInvalidBody extends Context {

    protected override def urlSuffix: String = "csars/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = scala.io.Source.fromFile("./test/tosca/appplusdb_bad.csar").mkString
      Some(new String(contentToEncode))
    }
    protected override def headersOpt: Option[Map[String, String]] = None

    private val post = POST(url)(httpClient)
      .addHeaders(headers)
      .addBody(body)

    def succeeds: SpecsResult = {
      val resp = execute(post)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.ServiceUnavailable)
    }
  }

} */
