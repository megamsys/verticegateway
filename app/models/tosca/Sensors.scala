/*
** Copyright [2013-2016] [Megam Systems]
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

package models.tosca

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import cache._
import db._
import models.json.tosca._
import models.json.sensors._
import controllers.Constants._
import io.megam.auth.funnel.FunnelErrors._
import app.MConfig
import models.base._

import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import io.megam.common.riak.GunnySack
import io.megam.util.Time
import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author ranjitha
 *
 */
case class Payload(accounts_id: String, assemblies_id: String, assembly_id: String, component_id: String, state: String, source: String, node: String, message: String, audit_period_begining: String, audit_period_ending: String, metrics: MetricList) {

  val json = "{\"accounts_id\":\"" + accounts_id + "\",\"assemblies_id\":\"" + assemblies_id + "\",\"assembly_id\":\"" + assembly_id + "\",\"component_id\":\"" + component_id + "\",\"state\":\"" + state + "\",\"source\":\"" + source + "\",\"node\":\"" + node + "\",\"message\":\"" + message + "\",\"audit_period_begining\":\"" + audit_period_begining + "\",\"audit_period_ending\":\"" + audit_period_ending + "\",\"metrics\":" + MetricList.toJson(metrics, true) + "}"

}

object Payload {
  def empty: Payload = new Payload(new String(), new String(), new String(), new String(), new String(), new String(), new String(), new String(), new String(), new String(), MetricList.empty)

}

case class SensorsInput(sensor_type: String, payload: Payload) {
  val json = "{\"sensor_type\":\"" + sensor_type + "\",\"payload\":" + payload.json + "}"

}

case class SensorsResult(id: String, sensor_type: String, payload: Payload, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new SensorsResultSerialization()
    toJSON(this)(preser.writer) //where does this JSON from?
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object SensorsResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[SensorsResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    val preser = new SensorsResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[SensorsResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

case class Metric(metric_type: String, metric_value: String, metric_units: String, metric_name: String) {
  val json = "{\"metric_type\":\"" + metric_type + "\",\"metric_value\":\"" + metric_value + "\", \"metric_units\":\"" + metric_units + "\", \"metric_name\":\"" + metric_name + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new MetricSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }

}

object Metric {
  def empty: Metric = new Metric(new String(), new String(), new String(), new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[Metric] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    val preser = new MetricSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[Metric] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }
}

case class Sensors(sensor_type: String, payload: Payload) {
  val json = "{\"sensor_type\":\"" + sensor_type + "\",\"payload\":" + payload.json + "}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new SensorsSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object Sensors {
  implicit val formats = DefaultFormats
  private val riak = GWRiak("sensors")

  val metadataKey = "SENSORS"
  val metadataVal = "Sensors Creation"
  val bindex = "sensors"

  def empty: Sensors = new Sensors(new String(), Payload.empty)

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[Sensors] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    val preser = new SensorsSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[Sensors] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    val sensorsInput: ValidationNel[Throwable, SensorsInput] = (Validation.fromTryCatchThrowable[SensorsInput, Throwable] {
      parse(input).extract[SensorsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      event <- sensorsInput
      aor <- (models.base.Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      uir <- (UID("snr").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(aor.get.id)
      //val bvalue = Set(event.a_id)
      val json = new SensorsResult(uir.get._1 + uir.get._2, event.sensor_type, event.payload, Time.now.toString).toJson(false)
      new GunnySack(uir.get._1 + uir.get._2, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }
  def create(email: String, input: String): ValidationNel[Throwable, Option[SensorsResult]] = {
    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[SensorsResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD,"Sensors created. success", Console.RESET))
              (parse(gs.get.value).extract[SensorsResult].some).successNel[Throwable];
            }
          }
        }
    }
  }

  def findById(sensorsID: Option[List[String]]): ValidationNel[Throwable, SensorsResults] = {
    (sensorsID map {
      _.map { asm_id =>
        play.api.Logger.debug(("%-20s -->[%s]").format("Sensors ID", asm_id))
        (riak.fetch(asm_id) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(asm_id, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              (Validation.fromTryCatchThrowable[SensorsResult, Throwable] {
                parse(xs.value).extract[SensorsResult]
              } leftMap { t: Throwable => new MalformedBodyError(xs.value, t.getMessage) }).toValidationNel.flatMap { j: SensorsResult =>
                play.api.Logger.debug(("%-20s -->[%s]").format("Sensors result", j))
                Validation.success[Throwable, SensorsResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ?
              }
            }
            case None => {
              Validation.failure[Throwable, SensorsResults](new ResourceItemNotFound(asm_id, "")).toValidationNel
            }
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((SensorsResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head.
  }

  def findByEmail(email: String): ValidationNel[Throwable, SensorsResults] = {
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Throwable, SensorsResults]] {
      (((for {
        aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      } yield {
        val bindex = ""
        val bvalue = Set("")
        play.api.Logger.debug(("%-20s -->[%s]").format("tosca.Sensors", "findByEmail" + aor.get.id))
        new GunnySack("sensors", aor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
          None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
      }) leftMap { t: NonEmptyList[Throwable] => t } flatMap {
        gs: Option[GunnySack] => riak.fetchIndexByValue(gs.get)
      } map { nm: List[String] =>
        (if (!nm.isEmpty) findById(nm.some) else
          new ResourceItemNotFound(email, "Sensors = nothing found for the user.").failureNel[SensorsResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    res.getOrElse(new ResourceItemNotFound(email, "Sensors = nothing found for the users.").failureNel[SensorsResults])
  }

}
