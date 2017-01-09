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

class BillingtransactionsSpec extends Specification {

  def is =
    "BillingtransactionsSpec".title ^ end ^ """
BillingtransactionsSpec is the implementation that calls the megam_play API server with the /billingtransactions url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do POST requests with an valid datas " ! create.succeeds ^
    //  "Correctly do GET requests with an valid datas " ! List.succeeds ^
    //  "Correctly do POST requests with an invalid key" ! PostInvalidUrl.succeeds ^
    //  "Correctly do POST requests with an invalid body" ! PostInvalidBody.succeeds ^
      end

  case object create extends Context {

    protected override def urlSuffix: String = "billingtransactions/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
        "\"accounts_id\": \"5555555\"," +
        "\"gateway\":\"paypal\"," +
        "\"amountin\": \"20\"," +
        "\"amountout\":\"5.99\"," +
        "\"fees\": \"0.99\"," +
        "\"tranid\": \"HGH111\"," +
        "\"trandate\": \"31/21/2012\"," +
        "\"currency_type\":\"USD\"," +
        "\"inputs\":[{\"key\":\"quota_based\",\"value\":\"false\"}]," +
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

  case object List extends Context {
    protected override def urlSuffix: String = "billingtransactions"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object PostInvalidUrl extends Context {

    protected override def urlSuffix: String = "billingtransactions/contentinvalidurl"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
        "\"id\": \"\"," +
        "\"accounts_id\": \"ACT000001\"," +
        "\"gateway\":\"paypal\"," +
        "\"amountin\": \"10\"," +
        "\"amountout\":\"5.99\"," +
        "\"fees\": \"0.99\"," +
        "\"tranid\": \"HGH111\"," +
        "\"trandate\": \"31/21/2012\"," +
        "\"currency_type\":\"USD\"," +
        "\"created_at\": \"\"," +
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

  case object PostInvalidBody extends Context {

    protected override def urlSuffix: String = "billingtransactions/content"

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
