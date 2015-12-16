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
package spark

import scalaz._
import scalaz.NonEmptyList._
import scalaz.Validation
import scalaz.Validation._
import Scalaz._
import controllers.Constants._
import play.api.libs.json._
/**
 *
 * @author ranjitha
 *
 */
object JobSubmittedSerialization {

  def decode(code: Int, jsonstr: String): JobSubmitted = {
    val json = Json.parse(jsonstr)
    val status = (json \ "status").asOpt[String]
    val resultstr = (json \ "result").asOpt[String]
    val job_id = (json \ "result" \ "jobId").asOpt[String]
    val context = (json \ "result" \ "context").asOpt[String]
    val message = (json \ "result" \ "message").asOpt[String]
    val errorClaz = (json \ "result" \ "errorClass").asOpt[String]

    new JobSubmitted(code, status.getOrElse(""),
      resultstr.getOrElse(""),
      new JobResult(message.getOrElse(""),
       job_id.getOrElse(""),
       context.getOrElse(""),
       errorClaz.getOrElse("")))
  }

}
