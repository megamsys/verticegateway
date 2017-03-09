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

class BillingsSpec extends Specification {

  def is =
    "BillingsSpec".title ^ end ^ """
  BillingsSpec is the implementation that calls the megam_play API server with the /billings url
  """ ^ end ^
      "The Client Should" ^
    //  "Correctly do POST  requests with an valid datas for update balance" ! balance_update.succeeds ^
    //  "Correctly do POST  requests with an valid datas for create quota with unpaid state" ! create_quota.succeeds ^
    //  "Correctly do POST  requests with an valid datas for update quota with paid state" ! quota_paid.succeeds ^
    //  "Correctly do POST  requests with an valid datas for update existing quota with unpaid state" ! quota_unpaid.succeeds ^
      "Correctly do POST  requests with an valid datas for deduct balance" ! balance_deduct.succeeds ^
      end

  case object balance_update extends Context {

    protected override def urlSuffix: String = "billings/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
       "\"key\":\"AddFunds\","+
       "\"name\":\"\","+
       "\"allowed\":[],"+
       "\"inputs\":[],"+
       "\"quota_type\":\"\","+
       "\"status\":\"\","+
       "\"orderid\":\"\","+
       "\"gateway\":\"offlinecc\","+
       "\"amount\":\"10.00\","+
       "\"trandate\":\"09/03/2017\","+
       "\"currency_type\":\"USD\"}"

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

  case object create_quota extends Context {

    protected override def urlSuffix: String = "billings/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
      "\"key\":\"Hosting\","+
      "\"name\":\"cloud-m-01\","+
      "\"allowed\":[{\"key\":\"RAM\",\"value\":\"2 GB\"},{\"key\":\"CPU\",\"value\":\"2 Core\"},{\"key\":\"DISK\",\"value\":\"20 GB\"},{\"key\":\"DISK_TYPE\",\"value\":\"HDD\"},{\"key\":\"BANDWIDTH\",\"value\":\"8 TB\"}],"+
      "\"inputs\":[],"+
      "\"quota_type\":\"VM\","+
      "\"status\":\"Unpaid\","+
      "\"orderid\":\"293\","+
      "\"gateway\":\"offlinecc\","+
      "\"amount\":\"80.00\","+
      "\"trandate\":\"09/03/2017\","+
      "\"currency_type\":\"USD\"}"
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

  case object quota_paid extends Context {
    protected override def urlSuffix: String = "billings/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
      "\"key\":\"Hosting\","+
      "\"name\":\"cloud-m-01\","+
      "\"allowed\":[{\"key\":\"RAM\",\"value\":\"2 GB\"},{\"key\":\"CPU\",\"value\":\"2 Core\"},{\"key\":\"DISK\",\"value\":\"20 GB\"},{\"key\":\"DISK_TYPE\",\"value\":\"HDD\"},{\"key\":\"BANDWIDTH\",\"value\":\"8 TB\"}],"+
      "\"inputs\":[],"+
      "\"quota_type\":\"VM\","+
      "\"status\":\"Paid\","+
      "\"orderid\":\"293\","+
      "\"gateway\":\"offlinecc\","+
      "\"amount\":\"80.00\","+
      "\"trandate\":\"09/03/2017\","+
      "\"currency_type\":\"USD\"}"
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

  case object quota_unpaid extends Context {

    protected override def urlSuffix: String = "billings/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
      "\"key\":\"Hosting\","+
      "\"name\":\"cloud-m-01\","+
      "\"allowed\":[{\"key\":\"RAM\",\"value\":\"2 GB\"},{\"key\":\"CPU\",\"value\":\"2 Core\"},{\"key\":\"DISK\",\"value\":\"20 GB\"},{\"key\":\"DISK_TYPE\",\"value\":\"HDD\"},{\"key\":\"BANDWIDTH\",\"value\":\"8 TB\"}],"+
      "\"inputs\":[],"+
      "\"quota_type\":\"VM\","+
      "\"status\":\"Unpaid\","+
      "\"orderid\":\"293\","+
      "\"gateway\":\"offlinecc\","+
      "\"amount\":\"80.00\","+
      "\"trandate\":\"09/03/2017\","+
      "\"currency_type\":\"USD\"}"
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

  case object balance_deduct extends Context {

    protected override def urlSuffix: String = "billings/content"

    protected override def bodyToStick: Option[String] = {
      val contentToEncode = "{" +
       "\"key\":\"Item\","+
       "\"name\":\"\","+
       "\"allowed\":[],"+
       "\"inputs\":[],"+
       "\"quota_type\":\"\","+
       "\"status\":\"\","+
       "\"orderid\":\"\","+
       "\"gateway\":\"offlinecc\","+
       "\"amount\":\"320.00\","+
       "\"trandate\":\"09/03/2017\","+
       "\"currency_type\":\"USD\"}"

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
