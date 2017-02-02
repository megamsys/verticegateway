/*
 * @author rajthilak
 */

package test

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import com.stackmob.newman.response._
import org.specs2.matcher.{ MatchResult, Expectable, Matcher }
import org.specs2.execute.{ Failure => SpecsFailure, Result => SpecsResult }
import net.liftweb.json.scalaz.JsonScalaz._
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import org.apache.commons.codec.binary.Base64
import com.stackmob.newman._
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import com.stackmob.newman.dsl._
import scala.concurrent.Await
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.Await
import java.net.URL
import java.util.Calendar
import java.text.SimpleDateFormat
import io.megam.auth.stack.HeaderConstants._
import io.megam.auth.stack.GoofyCrypto._

trait BaseContext {

  val X_Megam_EMAIL = "X-Megam-EMAIL"
  val X_Megam_APIKEY = "X-Megam-APIKEY"
  val X_Megam_DATE = "X-Megam-DATE"
  val X_Megam_ORG = "X-Megam-ORG"
  val X_Megam_PUTTUSAVI = "X-Megam-PUTTUSAVI"
  val X_Megam_PASSWORD = "X-Megam-PASSWORD"
  val X_Megam_MASTER_KEY = "X-Megam-MASTER-KEY"
  val X_Megam_MASTERKEY = "X-Megam-MASTERKEY"
  val Content_Type = "Content-Type"
  val application_json = "application/json"
  val Accept = "Accept"
  val application_vnd_megam_json = "application/vnd.megam+json"

  val currentDate = new SimpleDateFormat("yyy-MM-dd HH:mm") format Calendar.getInstance.getTime

  val defaultHeaderOpt = Map(Content_Type -> application_json,


    X_Megam_EMAIL -> "cd@ss.co", X_Megam_APIKEY -> "1189a21d8965ee670536cbb61fd9f5afed8489c0",
    X_Megam_ORG -> "ORG8385278424580953898",
    //X_Megam_EMAIL -> "megam@megam.io", X_Megam_APIKEY -> "IamAtlas{74}NobodyCanSeeME#07",

    //X_Megam_MASTERKEY -> "true", X_Megam_MASTER_KEY -> "3b8eb672aa7c8db82e5d34a0744740b20ed59e1f6814cfb63364040b0994ee3f",
  //X_Megam_PUTTUSAVI -> "true",  X_Megam_EMAIL -> "test@megam.io", X_Megam_PASSWORD -> "YWJj",
    X_Megam_DATE -> currentDate, Accept -> application_vnd_megam_json)

  protected class HeadersAreEqualMatcher(expected: Headers) extends Matcher[Headers] {
    override def apply[S <: Headers](r: Expectable[S]): MatchResult[S] = {
      val other: Headers = r.value
      val res = expected === other
      result(res, "Headers are equal", expected + " does not equal " + other, r)
    }
  }

  protected class HttpResponseCodeAreEqualMatcher(expected: HttpResponseCode = HttpResponseCode.Ok) extends Matcher[HttpResponseCode] {
    override def apply[S <: HttpResponseCode](r: Expectable[S]): MatchResult[S] = {
      val other: HttpResponseCode = r.value
      val res = (expected === other)
      result(res, "HttpResponse core are equal", expected + " does not equal " + other, r)
    }
  }

  protected class HttpResponsesAreEqualMatcher(expected: HttpResponse) extends Matcher[HttpResponse] {
    override def apply[S <: HttpResponse](r: Expectable[S]): MatchResult[S] = {
      val other: HttpResponse = r.value
      val res = (expected.code === other.code) && (expected.headers === other.headers) && (expected.bodyString === other.bodyString)
      result(res, "HttpResponses are equal", expected + " does not equal " + other, r)
    }
  }

  protected def haveTheSameHeadersAs(h: Headers) = new HeadersAreEqualMatcher(h)

  protected def beTheSameResponseAs(h: HttpResponse) = new HttpResponsesAreEqualMatcher(h)

  protected def beTheSameResponseCodeAs(h: HttpResponseCode) = new HttpResponseCodeAreEqualMatcher(h)

  protected def logAndFail(t: Throwable): SpecsResult = {
    SpecsFailure("failed with exception %s".format(t.getMessage))
  }

  private def errorString(err: Error) = err match {
    case UnexpectedJSONError(was, expected) => "unexpected JSON. was %s, expected %s".format(was.toString, expected.toString)
    case NoSuchFieldError(name, json) => "no such field %s in json %s".format(name, json.toString)
    case UncategorizedError(key, desc, args) => "uncategorized JSON error for key %s: %s (args %s)".format(key, desc, args.mkString("&"))
  }

  protected def logAndFail(errs: NonEmptyList[Error]): SpecsResult = {
    val totalErrString = errs.map(errorString(_)).list.mkString("\n")
    SpecsFailure("JSON errors occurred: %s".format(totalErrString))
  }

  protected def sandboxHeaderAndBody(contentToEncodeOpt: Option[String],
    headerOpt: Option[Map[String, String]], path: String): (Headers, RawBody) = {
    val headerMap: Map[String, String] = headerOpt.getOrElse(defaultHeaderOpt)

    val signWithHMAC = headerMap.getOrElse(X_Megam_DATE, currentDate) + "\n" + path + "\n" + toMD5(contentToEncodeOpt).get
    play.api.Logger.debug("%-20s -->[%s]".format("SIGN", signWithHMAC))
    val puttusavi = headerMap.getOrElse(X_Megam_PUTTUSAVI, "blank_key")
    val masterkey = headerMap.getOrElse(X_Megam_MASTERKEY, "blank_key")

    if (puttusavi == "true") {

      val signedWithHMAC = toHMAC((headerMap.getOrElse(X_Megam_PASSWORD, "blank_key")), signWithHMAC)
      val finalHMAC = headerMap.getOrElse(X_Megam_EMAIL, "blank_email") + ":"+ signedWithHMAC
      (Headers((headerMap + (X_Megam_HMAC -> finalHMAC)).toList),
        RawBody(contentToEncodeOpt.getOrElse(new String())))

    } else if(masterkey == "true") {

      val signedWithHMAC = toHMAC((headerMap.getOrElse(X_Megam_MASTER_KEY, "blank_key")), signWithHMAC)
      val finalHMAC = headerMap.getOrElse(X_Megam_EMAIL, "blank_email") + ":" + signedWithHMAC

      (Headers((headerMap + (X_Megam_HMAC -> finalHMAC)).toList),
        RawBody(contentToEncodeOpt.getOrElse(new String())))

    } else {

      val signedWithHMAC = toHMAC((headerMap.getOrElse(X_Megam_APIKEY, "blank_key")), signWithHMAC)
      val finalHMAC = headerMap.getOrElse(X_Megam_EMAIL, "blank_email") + ":" + signedWithHMAC

      (Headers((headerMap + (X_Megam_HMAC -> finalHMAC)).toList),
        RawBody(contentToEncodeOpt.getOrElse(new String())))
    }
  }
}

trait Context extends BaseContext {
  val httpClient = new ApacheHttpClient
  protected def urlSuffix: String
  protected def bodyToStick: Option[String] = Some(new String())
  protected def headersOpt: Option[Map[String, String]]
  lazy val url = new URL("http://localhost:9000/v2/" + urlSuffix)
  play.api.Logger.debug("<---------------------------------------->")
  play.api.Logger.debug("%-20s -->[%s]".format("MYURL", url))
  play.api.Logger.debug("%-20s -->[%s]".format("MYBODY", bodyToStick))

  val headerAndBody = sandboxHeaderAndBody(this.bodyToStick, headersOpt, url.getPath)

  protected val headers: Headers = headerAndBody._1
  protected val body = headerAndBody._2

  val h1 = headers.map { x => (for (j <- x.list) yield (j._1 + "=" + j._2)).mkString("\n", "\n", "") }

  implicit private val encoding = Constants.UTF8Charset

  protected def execute[T](t: Builder) = {
    val res = Await.result(t.apply, 30.second)
    play.api.Logger.debug("%-20s%n:%s".format("*** RESP RECVD", new String(res.bodyString)))
    play.api.Logger.debug("<---------------------------------------->")
    res
  }
}
