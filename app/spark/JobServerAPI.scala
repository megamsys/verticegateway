/**
 * * Copyright [2013-2015] [Megam Systems]
 * *
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * *
 * * http://www.apache.org/licenses/LICENSE-2.0
 * *
 * * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 */
package spark

import scalaz._
import Scalaz._
import java.net.URL
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import controllers.stack._
import scala.concurrent._

case class JarUpload(ji: JarsInput) extends JobServerClient {

  protected override def urlSuffix: String = "/jars/" + ji.uniqName

  protected override def bodyToStick: Option[Bytes] = asBytes.toOption

  protected override def headersOpt: Option[Map[String, String]] = None

  private val post = POST(url)(httpClient)
    .addHeaders(headers)
    .addBody(body)

  def run: Option[String] = execute(post).bodyString.some

  def asBytes: ValidationNel[Throwable, Bytes] = {
    val sourcesJar = new java.io.File(ji.location)
    val fileLength = sourcesJar.length()
    val bytes = new Array[Byte](fileLength.toInt)
    val sizeRead = try {
      val is = new java.io.BufferedInputStream(new java.io.FileInputStream(sourcesJar))
      val read = is.read(bytes)
      is.close()
      read
    } catch {
      case ex: java.io.IOException =>
        ("Failed to read built yonpi jar archive\n" + ex.toString()).failureNel
    }
    if (sizeRead != bytes.length) {
      ("Failed to read the built yonpi jar archive, size read: " + sizeRead).failureNel
    }
    bytes.successNel[Throwable]
  }
}

case class JarSubmit(ji: JarsInput) extends JobServerClient {
  protected override def urlSuffix: String = "/jobs?appName=" + ji.uniqName + "&classPath=" + ji.claz

  protected def headersOpt: Option[Map[String, String]] = None

  protected override def bodyToStick: Option[Bytes] = Some((ji.args.map { x => "input."+ x._1 + "=" + x._2 }).mkString(", ").getBytes)

  private val post = POST(url)(httpClient)
    .addHeaders(headers)
    .addBody(body)

  def run: Option[Tuple3[Int, String, String]] = {
    val response = execute(post)
    Tuple3(response.code.code, ji.uniqName, response.bodyString).some
  }

}

case class WbSubmit(ji: JarsInput) extends JobServerClient {
//  play.api.Logger.debug("%-20s -->[%s]".format("WbSubmit",  ji))

  protected override def urlSuffix: String = "/jobs?appName=" + ji.uniqName + "&classPath=" + ji.claz

  protected def headersOpt: Option[Map[String, String]] = None


  protected override def bodyToStick: Option[Bytes] = Some(({ "input.json " + "= " + ji.args.getOrElse(controllers.Constants.SPARKJOBSERVER_INPUT, "")}).mkString("").getBytes)



 //println({ "input.json " + "= " + ji.args.getOrElse(controllers.Constants.SPARKJOBSERVER_INPUT, "")}).mkString(""))


 private val post = POST(url)(httpClient)
    .addHeaders(headers)
    .addBody(body)


  def run: Option[Tuple3[Int, String, String]] = {
    play.api.Logger.debug("%-20s -->[%s]".format("run",  "Calling SJS"))




    val response = execute(post)
    Tuple3(response.code.code, ji.uniqName, response.bodyString).some

  }

}
case class JobResults(ji: JobsInput) extends JobServerClient {
  protected override def urlSuffix: String = "/jobs/" + ji.id

  protected def headersOpt: Option[Map[String, String]] = None

  Thread.sleep(30000) //This is very ugly fix.
  
  private val get = GET(url)(httpClient)
    .addHeaders(headers)

  def run: Option[String] = execute(get).bodyString.some
}
