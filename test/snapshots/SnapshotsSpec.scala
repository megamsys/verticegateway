package test.snapshots
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

class SnapshotsSpec extends Specification {

  def is =
    "SnapshotsSpec".title ^ end ^ """
  SnapshotsSpec is the implementation that calls the megam_play API server with the /snapshots url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do POST snapshots with a valid userid and api key" ! Post.succeeds ^
   "Correctly do GET  requests with an valid valid Assembly ID" ! Get.succeeds ^
   //"Correctly do LIST requests with a valid Accounts ID" ! List.succeeds ^
  end

  case object Post extends Context {

    protected override def urlSuffix: String = "snapshots/content"

    protected override def bodyToStick: Option[String] = {
    val contentToEncode = "{" +
      "\"asm_id\": \"ASM5355764237644862\"," +
      "\"org_id\":\"ORG787966332632133744\"," +
      "\"account_id\": \"\"," +
      "\"name\":\"rrr.megambox.com\"," +
      "\"status\":\"progress44\"," +
      "\"tosca_type\":\"xxxxx\"," +
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


  case object List extends Context {
    protected override def urlSuffix: String ="snapshots"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object Get extends Context {
      protected override def urlSuffix: String ="snapshots/ASM5355764237644862"

      protected def headersOpt: Option[Map[String, String]] = None
      private val get = GET(url)(httpClient)
        .addHeaders(headers)
      def succeeds = {
        val resp = execute(get)
        resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
      }
    }

}
