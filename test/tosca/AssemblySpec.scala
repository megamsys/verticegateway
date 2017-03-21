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
import models.tosca.Assembly
import test.{ Context }

class AssemblySpec extends Specification {

  def is =
    "AssemblySpec".title ^ end ^ """
  AssemblySpec is the implementation that calls the megam_play API server with the /assembly url
  """ ^ end ^
      "The Client Should" ^
      //"Correctly do GET  requests with an valid Assembly ID" ! findByIDAppNotFound.succeeds ^
      //"Correctly do POST requests with an valid Assembly ID" ! updateAppNotFound.succeeds ^
      "Correctly do POST requests with an valid Assembly ID" ! updateAssembly.succeeds ^
      //"Correctly do GET requests with an valid Assembly ID" ! upgradeAppNotFound.succeeds ^
      //"Correctly do GET  requests with an valid valid email id" ! List.succeeds ^
      end


  case object findByIDAppNotFound extends Context {
    protected override def urlSuffix: String = "assembly/ASM9070050271024385313"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.NotFound)
    }
  }

  case object updateAppNotFound extends Context {

    protected override def urlSuffix: String = "assembly/update"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
        "\"id\":\"ASM4669538364206151823\"," +
        "\"org_id\":\"ORG123\"," +
        "\"name\":\"calcines\"," +
        "\"components\":[\"COM1282015862571794432\"]," +
        "\"tosca_type\":\"tosca.torpedo.coreos\"," +
        "\"policies\":[{ " +
        "\"name\":\"bind policy\"," +
        "\"ptype\":\"colocated\"," +
        "\"members\":[\"calcines.megam.co/MattieGarcia\",\"calcines.megam.co/parsnip\"]" +
        "}]," +
        "\"inputs\":[" +
        "{\"key\":\"domain\",\"value\":\"megam.co\"}," +
        "{\"key\":\"source\",\"value\":\"dfghfh\"}," +
        "]," +
        "\"outputs\":[]," +
        "\"status\":\"Launching\"," +
        "\"state\":\"Launching\"," +
        "\"created_at\":\"2014-10-29 13:24:06 +0000\"" +
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

  case object updateAssembly extends Context {

    protected override def urlSuffix: String = "assembly/update"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
        "\"id\":\"ASM7060615123730378474\"," +
        "\"org_id\":\"ORG6121284762420561738\"," +
        "\"name\":\"weputpeople\"," +
        "\"components\":[\"COM4947420119639501688\"]," +
        "\"tosca_type\":\"tosca.torpedo.windows\"," +
        "\"policies\":[]," +
        "\"inputs\":[" +
        "{\"key\":\"domain\",\"value\":\"raid1.host\"}," +
        "{\"key\":\"keypairoption\",\"value\":\"1\"}," +
        "{\"key\":\"root_password\",\"value\":\"ZGpiYWJ5MTIz\"}," +
        "{\"key\":\"sshkey\",\"value\":\"\"}," +
        "{\"key\":\"provider\",\"value\":\"one\"}," +
        "{\"key\":\"cpu\",\"value\":\"1 Cores\"}," +
        "{\"key\":\"ram\",\"value\":\"2 GB\"}," +
        "{\"key\":\"hdd\",\"value\":\"25 GB\"}," +
        "{\"key\":\"version\",\"value\":\"\"}," +
        "{\"key\":\"region\",\"value\":\"Strasbourg\"}," +
        "{\"key\":\"resource\",\"value\":\"Raid1\"}," +
        "{\"key\":\"storage_hddtype\",\"value\":\"SSD\"}," +
        "{\"key\":\"private_ipv4\",\"value\":\"false\"}," +
        "{\"key\":\"public_ipv4\",\"value\":\"true\"}," +
        "{\"key\":\"private_ipv6\",\"value\":\"false\"}," +
        "{\"key\":\"public_ipv6\",\"value\":\"false\"}," +
        "{\"key\":\"bitnami_password\",\"value\":\"true\"}," +
        "{\"key\":\"bitnami_username\",\"value\":\"true\"}," +
        "{\"key\":\"app_username\",\"value\":\"\"}," +
        "{\"key\":\"app_password\",\"value\":\"\"}," +
        "{\"key\":\"root_username\",\"value\":\"root\"}," +
        "{\"key\":\"backup\",\"value\":\"\"}," +
        "{\"key\":\"backup_name\",\"value\":\"\"}," +
        "{\"key\":\"vm_cpu_cost_per_hour\",\"value\":\"0.022222222222222223\"}," +
        "{\"key\":\"vm_ram_cost_per_hour\",\"value\":\"0.011111111111111112\"}," +
        "{\"key\":\"vm_disk_cost_per_hour\",\"value\":\"0.00044444444444444447\"}," +
        "{\"key\":\"container_cpu_cost_per_hour\",\"value\":\"0.01\"}," +
        "{\"key\":\"container_memory_cost_per_hour\",\"value\":\"0.02\"}," +
        "{\"key\":\"lastsuccessstatusupdate\",\"value\":\"04 Mar 17 06:40 EET\"}," +
        "{\"key\":\"status\",\"value\":\"error\"}" +
        "]," +
        "\"outputs\":["+
        "{\"key\":\"instance_id\",\"value\":\"208\"}," +
        "{\"key\":\"vncport\",\"value\":\"6108\"}," +
        "{\"key\":\"vnchost\",\"value\":\"compute1\"}," +
        "{\"key\":\"public_ipv4\",\"value\":\"193.70.18.59\"}" +
        "]," +
        "\"status\":\"stopped\"," +
        "\"state\":\"stopped\"," +
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

  case object upgradeAppNotFound extends Context {
    protected override def urlSuffix: String = "assembly/upgrade/ASM4669538364206151823"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.NotFound)
    }
  }

  case object List extends Context {
    protected override def urlSuffix: String = "assembly"

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)
    def succeeds = {
      val resp = execute(get)
      resp.code must beTheSameResponseCodeAs(HttpResponseCode.Ok)
    }
  }

}
