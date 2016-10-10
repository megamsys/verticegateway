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

class EventsContainerSpec extends Specification {

  def is =
    "EventsContainerSpec".title ^ end ^ """
  EventsContainerSpec is the implementation that calls the megam_play API server with the /events url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do POST  requests with a valid Assembly ID" ! Get.succeeds ^
      "Correctly do LIST requests with a valid Accounts ID" ! List.succeeds ^
      "Correctly do INDEX requests with a valid Accounts ID" ! Index.succeeds ^
      end

  case object List extends Context {
    protected override def urlSuffix: String ="eventscontainer/0"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object Index extends Context {
    protected override def urlSuffix: String ="eventscontainer"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object Get extends Context {
      protected override def urlSuffix: String ="eventscontainer/show/0"

      protected override def bodyToStick: Option[String] = {
        val contentToEncode = "{\"account_id\":\"\",\"created_at\":\"2016-05-05 10:57:30 +0000\",\"assembly_id\":\"ASM9038606864211614815\",\"event_type\":\"\",\"data\":[]}"
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
