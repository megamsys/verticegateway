/* 
** Copyright [2012-2013] [Megam Systems]
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
import scalaz.NonEmptyList
import Scalaz._
import play.api.http.Status._
import java.io.{ StringWriter, PrintWriter }

/**
 * @author ram
 *
 */
object FunnelErrors {

  val tailMsg =
    """
        |Ask for help  : https://groups.google.com/forum/?fromgroups=#!forum/megamlive. 
  		|Read our Docs : https://api.megam.co, http://docs.megam.co
        |Log a ticket  : http://support.megam.co""".stripMargin

  case class CannotAuthenticateError(input: String, msg: String, httpCode: Int = BAD_REQUEST)
    extends Error(msg)

  case class MalformedBodyError(input: String, msg: String, httpCode: Int = BAD_REQUEST)
    extends Error(msg)

  case class MalformedHeaderError(input: String, msg: String, httpCode: Int = NOT_ACCEPTABLE)
    extends Error(msg)

  case class ServiceUnavailableError(input: String, msg: String, httpCode: Int = SERVICE_UNAVAILABLE)
    extends Error(msg)

  case class ResourceItemNotFound(input: String, msg: String, httpCode: Int = NOT_FOUND)
    extends Error(msg)

  case class HttpReturningError(errNel: NonEmptyList[Throwable]) extends Exception {

     def mkMsg(err: Throwable): String = {
      err.fold(
        a => """Authentication failure using the email/apikey combination. %n'%s' 
            |Verify the email and api key combination. 
            """.format(a.input).stripMargin,
        m => """Body received from the API call contains invalid input. 'body:' %n'%s' 
            |Verify the body content as needed for this resource. 
            |""".format(m.input).stripMargin,
        h => """Header received from the API call contains invalid input. 'header:' %n'%s' 
            |Verify the header content as required for this resource. 
            |%s""".format(h.input).stripMargin,

        c => """Service unavailable. The layer responsible for fullfilling the request
            |came back with errors %n'%s'""".format(c.input).stripMargin,
        r => """The resource requested wasn't found  <.!.>  '%s' 
            |											 ( ^ )
            |                                 		      ~~~   
            |""".format(r.input).stripMargin,

        t => """Ooops ! I know its crazy. We flunked. 
            |Contact support with this text.                   
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
      err.fold(a => tailMsg,
        m => """|The error received when parsing the JSON is :
    		  	|%s%n%s""".format(m.msg, tailMsg).stripMargin,
        h => tailMsg,
        c => """|The error received from the service :
    		  	|%s%n%s""".format(c.msg, tailMsg).stripMargin,
        r => """|The error received from the datasource :
    		  	|%s%n%s""".format(r.msg, tailMsg).stripMargin,
        t => """|Pardon us. This is how it happened.             
            |Stack trace 
            |%s
    		|%s
            """.format({ val u = new StringWriter; t.printStackTrace(new PrintWriter(u)); u.toString }, tailMsg).stripMargin)
    }

    def more: Option[String] = { errNel.map { err: Throwable => mkMore(err) }.list.mkString("\n").some }

    def severity = { "error" }    
    
  }

  

}