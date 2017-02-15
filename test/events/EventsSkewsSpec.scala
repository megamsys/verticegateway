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

class EventsSkewsSpec extends Specification {

def is =
  "EventsSkewsSpec".title ^ end ^ """
EventsSkewsSpec is the implementation that calls the megam_play API server with the /events url
""" ^ end ^
    "The Client Should" ^
    "Correctly do POST  requests with a valid Assembly ID" ! Post.succeeds ^
   "Correctly do LIST requests with a valid Accounts ID" ! List.succeeds ^
    "Correctly do GET requests with a valid Accounts ID" ! Get.succeeds ^
    end

case object List extends Context {
  protected override def urlSuffix: String ="eventsskews"

  protected def headersOpt: Option[Map[String, String]] = None

  private val get = GET(url)(httpClient)
    .addHeaders(headers)
  def succeeds = {
    val resp = execute(get)
    resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
  }
}

case object Get extends Context {
  protected override def urlSuffix: String ="eventsskews/ASM5355764237644863"

  protected def headersOpt: Option[Map[String, String]] = None

  private val get = GET(url)(httpClient)
    .addHeaders(headers)
  def succeeds = {
    val resp = execute(get)
    resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
  }
}

case object Post extends Context {

  protected override def urlSuffix: String = "eventsskews/content"

  protected override def bodyToStick: Option[String] = {
  val contentToEncode = "{" +
    "\"account_id\": \"\"," +
    "\"cat_id\": \"ASM5355764237644864\"," +
    "\"event_type\":\"vm\"," +
    "\"inputs\":[" +
    "{\"key\":\"generated_at\",\"value\":\"xxxxx\"}," +
    "{\"key\":\"last_due_date\",\"value\":\"yyyyy\"}," +
    "]," +
    "\"actions\":[" +
    "{\"key\":\"next_action\",\"value\":\"eeeeee\"}," +
    "{\"key\":\"last_action_done\",\"value\":\"dfghfh\"}," +
    "]," +
    "\"status\": \"billing\"," +
    "}"
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

}
