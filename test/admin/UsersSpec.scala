package test.admin

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

class UsersSpec extends Specification {

  def is =
    "UsersSpec".title ^ end ^ """
  UsersSpec is the implementation that calls the megam_play API server with the /accounts url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do GET list of users" ! Get.succeeds ^
      "Correctly do GET list fails with a invalid authority" ! GetInvalidAuthority.succeeds ^
      end


  case object Get extends Context {
    protected override def urlSuffix: String = "accounts"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

  case object GetInvalidAuthority extends Context {
    protected override def urlSuffix: String = "accounts"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.NotFound)
    }
  }

}
