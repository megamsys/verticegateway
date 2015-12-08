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

import java.net.URL
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import controllers.stack._


  case class JarUpload(input: JarsInput) extends JobServerClient {

    protected override def urlSuffix: String = "jars/" + input.name

    protected override def bodyToStick: Option[Bytes] = Some(input.jars.getBytes())

    protected override def headersOpt: Option[Map[String, String]] = None

    private val post = POST(url)(httpClient)
      .addHeaders(headers)
      .addBody(body)

    def run =  execute(post)

  }


  case class JobResults(job: JobsInput) extends JobServerClient {
    protected override def urlSuffix: String = "jobs/" + job.id

    protected def headersOpt: Option[Map[String, String]] = None

    private val get = GET(url)(httpClient)
      .addHeaders(headers)

    def run =  execute(get)
  }
