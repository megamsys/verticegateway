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

package test

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
import scala.concurrent._
import scala.concurrent.duration._
import java.net.URL
import io.megam.auth.stack.HeaderConstants._
import models.base._
import test._

class WorkbenchesSpec extends Specification {
  def is =
    "WorkbenchesSpec".title ^ end ^
      """
      WorkbenchesSpec is the implementation that calls the API server with the /workbenches url to create workbenches
    """ ^ end ^
      "The Client Should" ^
    //  "Correctly do POST requests" ! Post0.succeeds ^ br ^
    //  "Correctly do GET workbenches with a valid userid and api key" ! Get.succeeds ^
      "Correctly do POST requests" ! Execute.succeeds ^ br ^
      end

  case object Post0 extends Context {

    protected override def urlSuffix: String = "workbenches/content"

    protected override def bodyToStick: Option[String] = {

      val contentToEncode = "{" +
        "\"id\":\"WOB1282015862542434304\"," +
        "\"name\": \"test2\"," +
        "\"connectors\":[{ " +
        "\"source\":\"mysql\"," +
        "\"endpoint\":\"103.56.92.47\"," +
        "\"port\":\"3306\"," +
        "\"dbname\":\"fooDatabase\"," +
        "\"inputs\":["+
        "{\"key\":\"name\",\"value\":\"cocdb\"}," +
        "{\"key\":\"username\",\"value\":\"fooUser\"}," +
        "{\"key\":\"password\",\"value\":\"megam\"}" +
        "]," +
        "\"tables\":[{ " +
        "\"name\":\"product sales\"," +
        "\"table_id\":\"sn145\"," +
        "\"schemas\":[{\"key\":\"cccc\", \"value\" :\"ssss\"}]," +
        "\"links\":[{\"key\":\"ddd\", \"value\" : \"tttt\"}]" +
        "}]" +
        "}]," +
        "\"created_at\":\"2014-10-29 13:24:06 +0000\"" +
        "}"
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
case object Get extends Context {
    protected override def urlSuffix: String = "workbenches/foo"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)

    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object Execute extends Context {

    protected override def urlSuffix: String = "workbenches/execute"

    protected override def bodyToStick: Option[String] = {

      val contentToEncode = "{" +
        "\"name\": \"test2\"," +
        "\"query\": \"product_id year \"" +
      "}"
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
}
