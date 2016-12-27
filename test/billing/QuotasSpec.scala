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

class QuotasSpec extends Specification {

  def is =
    "QuotasSpec".title ^ end ^ """
  QuotasSpec is the implementation that calls the megam_play API server with the /quotas url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do POST  requests with an valid datas" ! create.succeeds ^
      //"Correctly do POST requests with an invalid key" ! PostInvalidUrl.succeeds ^
      //"Correctly do POST requests with an invalid body" ! PostInvalidBody.succeeds ^
      //"Correctly do GET  requests with an valid valid email id" ! Get.succeeds ^
      end

  case object create extends Context {

    protected override def urlSuffix: String = "quotas/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
        "\"name\":\"cloud-s-02\"," +
        "\"account_id\":\"vino.v@megam.io\"," +
        "\"allowed\":["+
          "{\"key\":\"ram\",\"value\":\"4 GB\"}," +
          "{\"key\":\"cpu\",\"value\":\"2 Cores\"}," +
          "{\"key\":\"disk\",\"value\":\"60 GB\"}," +
          "{\"key\":\"disk_type\",\"value\":\"SSD\"}" +
         "],"+
         "\"allocated_to\":\"\","+
         "\"inputs\":[]" +
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

    protected override def urlSuffix: String = "quotas/contentinvalidurl"

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



}
