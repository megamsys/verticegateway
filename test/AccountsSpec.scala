package test

import org.specs2.mutable._
import org.specs2.Specification
import java.net.URL
import org.specs2.matcher.MatchResult
import org.specs2.execute.{ Result => SpecsResult }
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import controllers.stack._
import models.base._
import test._

class AccountsSpec extends Specification {

  def is =
    "AccountsSpec".title ^ end ^ """
  AccountsSpec is the implementation that calls the megam_play API server with the /accounts url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do POST requests with a valid userid and api key" ! Post.succeeds ^
      "Correctly do POST requests with an invalid key" ! PostInvalidUrl.succeeds ^
      "Correctly do POST requests with an invalid body" ! PostInvalidBody.succeeds ^
      "Correctly do GET requests with a valid userid and api key" ! Get.succeeds ^
      "Correctly do GET requests with a invalid apikey" ! GetInvalidApi.succeeds ^
      "Correctly do GET requests with a invalid email" ! GetInvalidEmail.succeeds ^
      //"Correctly do POST update requests with a valid userid and api_key" ! PostUpdate.succeeds ^
      end

  case object Post extends Context {

    protected override def urlSuffix: String = "accounts/content"

    protected override def bodyToStick: Option[String] = {
      //val contentToEncode = "{\"first_name\":\"Darth\", \"last_name\":\"Vader\", \"phone\":\"19090909090\", \"email\":\"rr@test.com\", \"api_key\":\"IamAtlas{74}NobodyCanSeeME#07\", \"password\":\"user\", \"authority\":\"user\", \"password_reset_key\":\"user\",\"password_reset_sent_at\":\"\" }"
      val contentToEncode = "{" +
            "\"name\":{" +
          "\"first_name\":\"Darth\"," +
          "\"last_name\":\"Vader\"" +
          "}," +
          "\"phone\":{" +
            "\"phone\":\"1998766366\"," +
            "\"phone_verified\":\"verified\"" +
            "}," +
            "\"email\":\"vert@test.com\"," +
            "\"api_key\":\"IamAtlas{74}NobodyCanSeeME#07\"," +
            "\"password\":{" +
        "\"password_hash\":\"user\"," +
        "\"password_reset_key\":\"user\"," +
        "\"password_reset_sent_at\":\"\"" +
        "}," +
        "\"states\":{" +
        "\"authority\":\"user\"," +
        "\"active\":\"active\"," +
        "\"blocked\":\"blocked\"," +
        "\"staged\":\"\"" +
        "}," +
        "\"approval\":{" +
        "\"approved\":\"approved\"," +
        "\"approved_by_id\":\"\"," +
        "\"approved_at\":\"\"" +
        "}," +
        "\"suspend\":{" +
        "\"suspended\":\"suspend\"," +
        "\"suspended_at\":\"\"," +
        "\"suspended_till\":\"\"" +
        "}," +
        "\"registration_ip_address\":\"\"," +
        "\"dates\":{" +
              "\"last_posted_at\":\"\"," +
              "\"last_emailed_at\":\"\"," +
              "\"previous_visit_at\":\"\"," +
              "\"first_seen_at\":\"\"," +
              "\"created_at\":\"\"" +
              "}" +
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

    protected override def urlSuffix: String = "accounts/contentinvalidurl"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{\"email\":\"tee@test.com\", \"api_key\":\"IamAtlas{74}NobodyCanSeeME#075488\", \"authority\":\"user\" }"
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

    protected override def urlSuffix: String = "accounts/content"

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

  case object Get extends Context {
    protected override def urlSuffix: String = "accounts/vertice123@test.com"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object GetInvalidApi extends Context {
    protected override def urlSuffix: String = "accounts/vertice123@test.com"

    protected override def headersOpt: Option[Map[String, String]] = Some(Map(Content_Type -> "Content-Type",
      X_Megam_EMAIL -> "vertice123@test.com", X_Megam_APIKEY -> "i@a)23_mC-han^00g57#ed8a+p%i",
      X_Megam_DATE -> "X-Megam-DATE", Accept -> application_vnd_megam_json))

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Unauthorized)
    }
  }

  case object GetInvalidEmail extends Context {
    protected override def urlSuffix: String = "accounts/#sandy007@megamsand.com"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.NotFound)
    }
  }

  case object PostUpdate extends Context {

    protected override def urlSuffix: String = "accounts/update"

    protected override def bodyToStick: Option[String] = {
      //val contentToEncode = "{\"id\":\"ACT4978057755611970607\",\"first_name\":\"Darth\", \"last_name\":\"moon\", \"phone\":\"435643656\", \"email\":\"tee@test.com\", \"api_key\":\"IamAtlas{74}NobodyCanSeeME#07\", \"password\":\"user\", \"authority\":\"user\", \"password_reset_key\":\"user\",\"password_reset_sent_at\":\"\",\"created_at\":\"2016-02-25 13:00:28 +0000\" }"
      val contentToEncode = "{" +
           "\"id\":\"ACT5367653782019016209\","+
            "\"name\":{" +
          "\"first_name\":\"Darth\"," +
          "\"last_name\":\"moon\"" +
          "}," +
          "\"phone\":{" +
            "\"phone\":\"4456778344\"," +
            "\"phone_verified\":\"verified\"" +
            "}," +
            "\"email\":\"vertice123@test.com\"," +
            "\"api_key\":\"IamAtlas{74}NobodyCanSeeME#07\"," +
            "\"password\":{" +
        "\"password_hash\":\"user\"," +
        "\"password_reset_key\":\"user\"," +
        "\"password_reset_sent_at\":\"\"" +
        "}," +
        "\"states\":{" +
        "\"authority\":\"user\"," +
        "\"active\":\"active\"," +
        "\"blocked\":\"blocked\"," +
        "\"staged\":\"\"" +
        "}," +
        "\"approval\":{" +
        "\"approved\":\"not\"," +
        "\"approved_by_id\":\"\"," +
        "\"approved_at\":\"\"" +
        "}," +
        "\"suspend\":{" +
        "\"suspended\":\"suspend\"," +
        "\"suspended_at\":\"\"," +
        "\"suspended_till\":\"\"" +
        "}," +
        "\"registration_ip_address\":\"\"," +
        "\"dates\":{" +
              "\"last_posted_at\":\"\"," +
              "\"last_emailed_at\":\"\"," +
              "\"previous_visit_at\":\"\"," +
              "\"first_seen_at\":\"\"," +
              "\"created_at\":\"\"" +
              "}" +
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

}
