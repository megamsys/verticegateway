package test.tosca

import org.specs2.mutable._
import org.specs2.Specification
import java.net.URL
import org.specs2.matcher.MatchResult
import org.specs2.execute.{ Result => SpecsResult }
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import models.tosca._
import models.team.Domains
import test.{ Context }

class DomainsSpec extends Specification {
  def is =
    "DomainsSpec".title ^ end ^ """
      DomainsSpec is the implementation that calls the megam_play API server with the /Domains url to create Domains
    """ ^ end ^
      "The Client Should" ^
      "Correctly do POST requests with a valid domains name" ! Post.succeeds ^
      "Correctly do POST requests with an invalid URL" ! PostInvalidUrl.succeeds ^
      "Correctly do POST requests with an invalid body" ! PostInvalidBody.succeeds ^
      "Correctly do GET requests with a valid orgid" ! Get.succeeds ^
      end

  case object Post extends Context {

    protected override def urlSuffix: String = "domains/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{\"name\":\"megambox.com\" }"
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

    protected override def urlSuffix: String = "domains/contentinvalidurl"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{\"name\":\"megam.org\"}"
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

    protected override def urlSuffix: String = "domains/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{\"name1\":\"megam.org\"}"
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

  case object Get extends Context {
    protected override def urlSuffix: String = "domains"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }
}
