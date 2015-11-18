/*
** Copyright [2013-2015] [Megam Systems]
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package controllers.funnel

import scalaz._
import Scalaz._
import java.io.{ StringWriter, PrintWriter }
import org.megam.common.jsonscalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import play.api.http.Status._

/**
 * @author ram
 *
 */
object FunnelErrors {

  val tailMsg =
    """Forum   :http://docs.megam.io/discuss
  	  |Docs    :http://docs.megam.io
      |Support :http://support.megam.io""".stripMargin

  case class CannotAuthenticateError(input: String, msg: String, httpCode: Int = BAD_REQUEST)
    extends java.lang.Error(msg)

  case class MalformedBodyError(input: String, msg: String, httpCode: Int = BAD_REQUEST)
    extends java.lang.Error(msg)

  case class MalformedHeaderError(input: String, msg: String, httpCode: Int = NOT_ACCEPTABLE)
    extends java.lang.Error(msg)

  case class ServiceUnavailableError(input: String, msg: String, httpCode: Int = SERVICE_UNAVAILABLE)
    extends java.lang.Error(msg)

  case class ResourceItemNotFound(input: String, msg: String, httpCode: Int = NOT_FOUND)
    extends java.lang.Error(msg)

  case class JSONParsingError(errNel: NonEmptyList[net.liftweb.json.scalaz.JsonScalaz.Error])
  extends java.lang.Error({
    errNel.map { err: net.liftweb.json.scalaz.JsonScalaz.Error =>
      err.fold(
        u => "unexpected JSON %s. expected %s".format(u.was.toString, u.expected.getCanonicalName),
        n => "no such field %s in json %s".format(n.name, n.json.toString),
        u => "uncategorized error %s while trying to decode JSON: %s".format(u.key, u.desc))
    }.list.mkString("\n")
  })

  case class HttpReturningError(errNel: NonEmptyList[Throwable]) extends Exception {

    def mkMsg(err: Throwable): String = {
      err.fold(
        a => """Authentication failure using the email/apikey combination. %n'%s'
            |verify the email and api key combination.
            """.format(a.input).stripMargin,
        m => """Body received from the api contains invalid input. 'body:' %n'%s'
            |verify the body content as needed for this resource.
            |""".format(m.input).stripMargin,
        h => """Header received from the api contains invalid input. 'header:' %n'%s'
            |verify the header content as required for this resource.
            |%s""".format(h.input).stripMargin,
        c => """Service layer failed to perform the the request
            |verify riak, snowflake or rabbitmq %n'%s'""".format(c.input).stripMargin,
        r => """The resource wasn't found   '%s'""".format(r.input).stripMargin,
        t => """Ooops ! I know its crazy. We flunked.
            |Contact support@megam.io with this text.
            """.format(t.getLocalizedMessage).stripMargin)
    }

    def msg: String = {
      errNel.map { err: Throwable => mkMsg(err) }.list.mkString("\n")
    }

    def mkCode(err: Throwable): Option[Int] = {
      err.fold(a => a.httpCode.some, m => m.httpCode.some, h => h.httpCode.some, c => c.httpCode.some,
        r => r.httpCode.some, t => INTERNAL_SERVER_ERROR.some)

    }

    def code: Option[Int] = { (errNel.map { err: Throwable => mkCode(err) }.list.head) }

    def mkMore(err: Throwable) = {
      err.fold(a => null,
        m => """|The error received when parsing the JSON is :
    		  	|%s""".format(m.msg).stripMargin,
        h => null,
        c => """|The error received from the service :
    		  	|%s""".format(c.msg).stripMargin,
        r => """|The error received from the datasource :
    		  	|%s""".format(r.msg).stripMargin,
        t => """|Pardon us. This is how it happened.
            |Stack trace
            |%s
            """.format({ val u = new StringWriter; t.printStackTrace(new PrintWriter(u)); u.toString }).stripMargin)
    }

    def more: Option[String] = { errNel.map { err: Throwable => mkMore(err) }.list.mkString("\n").some }

    def severity = { "error" }

  }

}
