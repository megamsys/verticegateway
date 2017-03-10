package test.billing

import org.specs2.mutable._
import org.specs2.Specification
import java.net.URL
import org.specs2.matcher.MatchResult
import org.specs2.execute.{ Result => SpecsResult }
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import test.{ Context }

class ExternalobjectsSpec extends Specification {

  def is =
    "ExternalobjectsSpec".title ^ end ^ """
  ExternalobjectsSpec is the implementation that calls the megam_play API server with the /external_objects url
  """ ^ end ^
      "The Client Should" ^
    //  "Correctly do POST  requests with an valid datas" ! create.succeeds ^
      "Correctly do GET  requests with an valid valid email id" ! Get.succeeds ^
      end

  case object create extends Context {

    protected override def urlSuffix: String = "external_objects/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
        "\"object_id\":\"4445\"," +
        "\"inputs\":["+
          "{\"key\":\"quota_id\",\"value\":\"QU00000\"}" +
         "],"+
        "}"

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

  case object Get extends Context {
      protected override def urlSuffix: String ="external_objects/4445"

      protected def headersOpt: Option[Map[String, String]] = None
      private val get = GET(url)(httpClient)
        .addHeaders(headers)
      def succeeds = {
        val resp = execute(get)
        resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
      }
    }



}
