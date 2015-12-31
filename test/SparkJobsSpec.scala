/**
 * * Copyright [2013-2015] [Megam Systems]
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
import controllers.stack.HeaderConstants._
import models.base._
import test._

class SparkJobsSpec extends Specification {
  def is =
    "SparkJobsSpec".title ^ end ^
      """
      SparkJobsSpec is the implementation that calls the API server with the /sparkjobs url to manage sparkjobs
    """ ^ end ^
      "The Client Should" ^
      "Correctly do upload jar into spark job server" ! Post0.succeeds ^
      "Correctly do GET sparkjob with a valid userid and api key" ! Get.succeeds ^
      end

  case object Post0 extends Context {

    protected override def urlSuffix: String = "sparkjobs/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
        "\"source\": \"https://github.com/megamsys/testsparkbb.git\"," +
        "\"inputs\": [" +
        "{\"key\":\"string\",\"value\":\"mera nam jokker thumhara nam kya hai\"}]," +
        "\"assembly_id\": \"ASM000001\"" +
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
    protected override def urlSuffix: String = "sparkjobs/47e01364-7741-4572-a438-b5f2be34fdfb"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)

    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

}
