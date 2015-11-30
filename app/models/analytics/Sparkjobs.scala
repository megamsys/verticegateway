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

package models.analytics

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import cache._
import db._
import models.json.tosca._
import models.json.tosca.carton._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import app.MConfig
import models.json.analytics._
import models.base._
import models.tosca._
import models.analytics._
import models.json.tosca.box._
import wash._

import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import org.megam.util.Time
import org.megam.common.riak.GunnySack
import org.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author ranjitha
 *
 */

case class Results(job_id: String, context: String) {
  val json = "{\"job_id\":\"" + job_id + "\",\"context\":\"" + context + "\"}"
}

object Results {
  def empty: Results = new Results(new String(), new String())
}

case class SparkjobsInput(inputs: models.tosca.KeyValueList, source: String) {
  val json = "{\"inputs\":" + KeyValueList.toJson(inputs, true) + ",\"source\":\"" + source + "\"}"

}

case class SparkjobsResult(id: String, inputs: models.tosca.KeyValueList, source: String, status: String, results: Results, created_at: String) {
  //val json = "{\"id\":\"" + id + "\",\"inputs\":" + models.tosca.KeyValueList.toJson(inputs, true) + ",\"source\":\"" + source + "\",\"status\":\"" + status + "\",\"results\":" + results.json + ",\"created_at\":\"" + Time.now.toString + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.analytics.SparkjobsResultSerialization
    val preser = new SparkjobsResultSerialization()
    toJSON(this)(preser.writer) //where does this JSON from?
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object SparkjobsResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[SparkjobsResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.analytics.SparkjobsResultSerialization
    val preser = new SparkjobsResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[SparkjobsResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Sparkjobs {

  implicit val formats = DefaultFormats
  private val riak = GWRiak("sparkjobs")
  val metadataKey = "sparkjobs"
  val metadataVal = "Sparkjobs Creation"
  val bindex = "sparkjobs"

  // A private method which chains computation to make GunnySack when provided with an input json, email.
  // After that flatMap on its success and the account id information is looked up.
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    val sparkjobsInput: ValidationNel[Throwable, SparkjobsInput] = (Validation.fromTryCatchThrowable[SparkjobsInput, Throwable] {
      parse(input).extract[SparkjobsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      sp <- sparkjobsInput
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "bal").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {

      val bvalue = Set(uir.get._1 + uir.get._2)
      val json = new SparkjobsResult(uir.get._1 + uir.get._2, sp.inputs, sp.source, "started", Results("", ""), Time.now.toString).toJson(false)
      //val json = "{\"id\": \"" + (uir.get._1 + uir.get._2) + "\",\"inputs\":" + KeyValueList.toJson(input.inputs, true) + ",\"source\": \"" + input.source + "\",\"status\": \"" + input.status + "\",\"results\": \"" + input.results.json + "\",\"created_at\":\"" + Time.now.toString + "\"}"
      new GunnySack(email, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  def create(email: String, input: String): ValidationNel[Throwable, Option[SparkjobsResult]] = {
    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[SparkjobsResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Sparkjobs.created success", Console.RESET))
              (parse(gs.get.value).extract[SparkjobsResult].some).successNel[Throwable];
            }
          }
        }
    }
  }

  def findByName(sparkjobsList: Option[List[String]]): ValidationNel[Throwable, SparkjobsResults] = {
    (sparkjobsList map {
      _.map { sparkjobsName =>
        play.api.Logger.debug("models.SparkjobsName findByName: Sparkjobs:" + sparkjobsName)
        (riak.fetch(sparkjobsName) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(sparkjobsName, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              (Validation.fromTryCatchThrowable[models.analytics.SparkjobsResult, Throwable] {
                parse(xs.value).extract[SparkjobsResult]
              } leftMap { t: Throwable =>
                new ResourceItemNotFound(sparkjobsName, t.getMessage)
              }).toValidationNel.flatMap { j: SparkjobsResult =>
                Validation.success[Throwable, SparkjobsResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ?
              }
            }
            case None => Validation.failure[Throwable, SparkjobsResults](new ResourceItemNotFound(sparkjobsName, "")).toValidationNel
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((SparkjobsResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head.

  }

}
