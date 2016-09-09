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
package test.events
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

class EventsVmSpec extends Specification {

  def is =
    "EventsVmSpec".title ^ end ^ """
  EventsVmSpec is the implementation that calls the megam_play API server with the /events url
  """ ^ end ^
      "The Client Should" ^
     //"Correctly do POST  requests with an valid Assembly ID" ! Get.succeeds ^
     "Correctly do LIST requests with a valid Accounts ID" ! List.succeeds ^
     //"Correctly do INDEX requests with a valid Accounts ID" ! Index.succeeds ^
      end

  case object List extends Context {
    protected override def urlSuffix: String ="eventsvm/0"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object Index extends Context {
    protected override def urlSuffix: String ="eventsvm"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object Get extends Context {
      protected override def urlSuffix: String ="eventsvm/show/0"

      protected override def bodyToStick: Option[String] = {
        val contentToEncode = "{\"account_id\":\"\",\"created_at\":\"\",\"assembly_id\":\"ASM01\",\"event_type\":\"\",\"data\":[]}"
        Some(contentToEncode)
      }

      protected def headersOpt: Option[Map[String, String]] = None
      private val get = POST(url)(httpClient)
        .addHeaders(headers)
        .addBody(body)

        def succeeds: SpecsResult = {
          val resp = execute(get)
          resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
        }

    }


}
