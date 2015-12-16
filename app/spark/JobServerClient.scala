/**
 * * Copyright [2013-2015] [Megam Systems]
 *
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
/*
 * @author rajthilak
 */

package spark

import com.stackmob.newman._
import com.stackmob.newman.response._
import com.stackmob.newman.dsl._
import scala.concurrent.Await
import scala.concurrent._
import scala.concurrent.duration._
import java.net.URL


//@parms jars :  is the jar file
//@parms name : is the name of the urlsuffix, its actually the last jarname with no extension.
case class JarsInput(prefix: String , location: String, name: String, args: Map[String,String] = Map.empty) {
  val claz: String = "io.megam.sparkbb.WordCountExample"
  val uniqName = prefix + "_" + name
}

case class JobsInput(id: String)

trait JobServerContext {

  type Bytes =  Array[Byte]

  val application_json = "application/json"

  val Content_Type = "Content-Type"

  val defaults = Map(Content_Type -> application_json)

  protected def HandB(b: Option[Bytes], h: Option[Map[String, String]]): (Headers, RawBody) = {
    val hM: Map[String, String] = h.getOrElse(defaults)
    (Headers(hM.toList), RawBody(b.getOrElse(new String().getBytes)))
  }
}

trait JobServerClient extends JobServerContext {

  val httpClient = new ApacheHttpClient

  protected def urlSuffix: String
  protected def bodyToStick: Option[Bytes] = None
  protected def headersOpt: Option[Map[String, String]]

  lazy val url = new URL(app.MConfig.spark_jobserver + urlSuffix)

  val headerAndBody = HandB(this.bodyToStick, headersOpt)

  protected val headers: Headers = headerAndBody._1

  protected val body = headerAndBody._2

  implicit private val encoding = Constants.UTF8Charset

  protected def execute[T](t: Builder) = Await.result(t.apply, Integer.parseInt(app.MConfig.yonpi_timeout).second)
}
