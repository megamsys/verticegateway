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

class EventsStorageSpec extends Specification {

def is =
  "EventsStorageSpec".title ^ end ^ """
EventsStorageSpec is the implementation that calls the megam_play API server with the /events url
""" ^ end ^
    "The Client Should" ^
    "Correctly do LIST requests with a valid Accounts ID" ! List.succeeds ^
    "Correctly do INDEX requests with a valid Accounts ID" ! Index.succeeds ^
    end

case object List extends Context {
  protected override def urlSuffix: String ="eventsstorage/0"

  protected def headersOpt: Option[Map[String, String]] = None

  private val get = GET(url)(httpClient)
    .addHeaders(headers)
  def succeeds = {
    val resp = execute(get)
    resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
  }
}

case object Index extends Context {
  protected override def urlSuffix: String ="eventsstorage"

  protected def headersOpt: Option[Map[String, String]] = None

  private val get = GET(url)(httpClient)
    .addHeaders(headers)
  def succeeds = {
    val resp = execute(get)
    resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
  }
}
}
