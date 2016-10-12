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
import models.tosca.Component
import test.{ Context }

class ComponentsSpec extends Specification {

  def is =
    "ComponentsSpec".title ^ end ^ """
  AssemblySpec is the implementation that calls the megam_play API server with the /assembly url
  """ ^ end ^
      "The Client Should" ^
      "Correctly do GET  requests with an valid Assembly ID" ! findByIDAppNotFound.succeeds ^
      //"Correctly do POST  requests with an valid Assembly ID" ! updateAppNotFound.succeeds ^
      end

  case object findByIDAppNotFound extends Context {
    protected override def urlSuffix: String = "components/COM7081623868492586469"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.NotFound)
    }
  }

  case object updateAppNotFound extends Context {

    protected override def urlSuffix: String = "components/update"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
        "\"id\": \"COM6497449919563135256\"," +
        "\"name\":\"NettieMoore\"," +
        "\"tosca_type\":\"tosca.web.redis\"," +
        "\"inputs\":[" +
        "{\"key\":\"domain\",\"value\":\"megam.co\"}," +
        "{\"key\":\"port\",\"value\":\"6379\"}," +
        "{\"key\":\"design_inputs\",\"value\":\"39bb18e7.c644e8\"}" +
        "]," +
        "\"outputs\":[]," +
        "\"envs\":[ " +
        "{\"key\":\"host\",\"value\":\"localhost\"}," +
        "{\"key\":\"port\",\"value\":\"8080\"}," +
        "{\"key\":\"username\",\"value\":\"admin\"}," +
        "{\"key\":\"password\",\"value\":\"admin\"}" +
        "]," +
        "\"artifacts\":{" +
        "\"artifact_type\":\"\"," +
        "\"content\":\"\"," +
        "\"artifact_requirements\":\"\"" +
        "}," +
        "\"related_components\":[\"AntonioMcCormick.megam.co/TimothyHenderson\"]," +
        "\"operations\":[" +
        "{" +
        "\"operation_type\":\"CI\"," +
        "\"description\":\"continous Integration\"," +
        "\"operation_requirements\":[" +
        "{\"key\":\"ci-scm\",\"value\":\"github\"}," +
        "{\"key\":\"ci-enable\",\"value\":\"true\"}," +
        "{\"key\":\"ci-token\",\"value\":\"token\"}," +
        "{\"key\":\"ci-owner\",\"value\":\"owner\"}" +
        "]" +
        "\"status\":\"notbound\"," +
        "}]," +
        "\"status\":\"\"," +
        "\"state\":\"Launching\"," +
        "\"repo\":{" +
        "\"rtype\":\"image\"," +
        "\"source\":\"github\"," +
        "\"oneclick\":\"yes\"," +
        "\"url\":\"imagename\"" +
        "}," +
        "\"created_at\":\"2014-10-29 14:06:39 +0000\"" +
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
