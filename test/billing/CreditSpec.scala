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

class CreditSpec extends Specification {

  def is =
    "CreditSpec".title ^ end ^ """
  CreditSpec is the implementation that calls the megam_play API server with the /credit url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do POST  requests with an valid datas" ! create.succeeds ^
      //"Correctly do POST requests with an invalid key" ! PostInvalidUrl.succeeds ^
      //"Correctly do POST requests with an invalid body" ! PostInvalidBody.succeeds ^
     "Correctly do GET  requests with an valid valid email id" ! Get.succeeds ^
     "Correctly do GET  requests with an valid valid email id" ! List.succeeds ^
      end

  case object create extends Context {

    protected override def urlSuffix: String = "credit/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
      "\"account_id\":\"rrr\"," +
        "\"credit\":\"456436\"" +
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

  case object PostInvalidUrl extends Context {

    protected override def urlSuffix: String = "credit/contentinvalidurl"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
        "\"credit\":\"456436\"" +
        "}"
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

  case object Get extends Context {
      protected override def urlSuffix: String ="credit/rrr"

      protected def headersOpt: Option[Map[String, String]] = None
      private val get = GET(url)(httpClient)
        .addHeaders(headers)
      def succeeds = {
        val resp = execute(get)
        resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
      }
    }

    case object List extends Context {
        protected override def urlSuffix: String ="credit"

        protected def headersOpt: Option[Map[String, String]] = None
        private val get = GET(url)(httpClient)
          .addHeaders(headers)
        def succeeds = {
          val resp = execute(get)
          resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
        }
      }

  case object PostInvalidBody extends Context {

    protected override def urlSuffix: String = "credit/content"

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
