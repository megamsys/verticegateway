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

case class SparkjobsInput(source: String, assembly_id: String, inputs: models.tosca.KeyValueList) {
  val json = "{\"source\":\"" + source + "\",\"assembly_id\":\"" + assembly_id + "\",\"inputs\":" + models.tosca.KeyValueList.toJson(inputs, true)+"}"
}

case class SparkjobsResult(id: String, code: Int, status: String, job_id: String, created_at: String) {
  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.analytics.SparkjobsResultSerialization
    val preser = new SparkjobsResultSerialization()
    toJSON(this)(preser.writer) //where does this JSON from?
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
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
  val metadataVal = "Sparkjobs create"
  val bindex = "assembly"

  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    
    val sparkjobsInput: ValidationNel[Throwable, SparkjobsInput] = (Validation.fromTryCatchThrowable[SparkjobsInput, Throwable] {
      parse(input).extract[SparkjobsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
    
    for {
      sp <- sparkjobsInput
      su <- spark.SparkSubmitter(sp).submit(false, email, KeyValueList.toMap(sp.inputs))
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "spj").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      println(sparkjobsInput)
      val bvalue = Set(sp.assembly_id)
      su flatMap {  so =>
      val json = new SparkjobsResult(uir.get._1 + uir.get._2, so._2.code, so._2.status, so._2.result.job_id, Time.now.toString).toJson(false)
        new GunnySack(so._1, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
      }
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

  def findById(sparkJob: Option[String]): ValidationNel[Throwable, Option[String]] = {
    sparkJob match {
      case Some(id) => spark.SparkSubmitter(new SparkjobsInput("","",KeyValueList.empty)).job(id)
      case None =>  new IllegalArgumentException("Missing job id").failureNel[Option[String]]
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
