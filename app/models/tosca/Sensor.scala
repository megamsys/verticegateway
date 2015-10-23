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

package models.tosca

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import org.megam.util.Time
import controllers.stack._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import models.tosca._
import models._
import models.cache._
import models.riak._
import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author ranjitha
 *
 */
 case class Payload(accounts_id: String, assemblies_id: String, assembly_id: String, component_id: String, state: String, source: String, node: String, message: String, audit_period_begining: String, audit_period_ending: String, metrics: KeyValueList) {

   val json = "{\"accounts_id\":\"" + accounts_id + "\",\"assemblies_id\":\"" + assemblies_id + "\",\"assembly_id\":\"" + assembly_id + "\",\"component_id\":\"" + component_id + "\",\"state\":\"" + state + "\",\"source\":\"" + source + "\",\"node\":\"" + node + "\",\"message\":\"" + message + "\",\"audit_period_begining\":\"" + audit_period_begining + "\",\"audit_period_ending\":\"" + audit_period_ending + "\",\"metrics\":" + KeyValueList.toJson(metrics, true) + "}"


  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.tosca.PayloadSerialization
    val preser = new models.json.tosca.PayloadSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }

}

object Payload {
  def empty: Payload = new Payload(new String(), new String(),  new String(),  new String() , new String(),  new String(),  new String() , new String(),  new String(),  new String(), KeyValueList.empty)

   def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[Payload] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.tosca.PayloadSerialization
    val preser = new models.json.tosca.PayloadSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[Payload] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue,Throwable] {
    play.api.Logger.debug(("%-20s -->[%s]").format("---json--->", json))
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}




case class SensorInput(id: String, sensor_type: String, payload:  models.tosca.PayloadList) {
  val json = "{\"id\":\"" + id + "\",\"sensor_type\":\"" + sensor_type + "\",\"payload\":\"" + models.tosca.PayloadList.toJson(payload, true) + "}"

}

case class SensorResult(id: String, sensor_type: String, payload:  models.tosca.PayloadList, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.tosca.SensorResultSerialization
    val preser = new SensorResultSerialization()
    toJSON(this)(preser.writer) //where does this JSON from?
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object SensorResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[SensorResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.tosca.SensorResultSerialization
    val preser = new SensorResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[SensorResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue,Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

case class Sensor(sensor_type: String, payload: models.tosca.PayloadList) {
  val json = "{\"sensor_type\":\"" + sensor_type + "\",\"payload\":" + models.tosca.PayloadList.toJson(payload, true) + "}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new models.json.tosca.SensorSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object Sensor {
  implicit val formats = DefaultFormats
  private val riak = GWRiak("sensor")

  val metadataKey = "SENSOR"
  val metadataVal = "Sensor Creation"
  val bindex = "sensor"

  def empty: Sensor = new Sensor(new String(), PayloadList.empty)

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[Sensor] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    val preser = new models.json.tosca.SensorSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[Sensor] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue,Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.tosca.Sensor", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val sensorInput: ValidationNel[Throwable, SensorInput] = (Validation.fromTryCatchThrowable[SensorInput,Throwable] {
      parse(input).extract[SensorInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      event <- sensorInput
     aor <- (models.Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "evt").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
     val bvalue = Set(aor.get.id)
       //val bvalue = Set(event.a_id)
      val json = new SensorResult(uir.get._1 + uir.get._2, event.sensor_type, event.payload, Time.now.toString).toJson(false)
      new GunnySack(uir.get._1 + uir.get._2, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }
  def create(email: String, input: String): ValidationNel[Throwable, Option[SensorResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Sensor", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[SensorResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("Sensor created. success", "Scaliak returned => None. Thats OK."))
              (parse(gs.get.value).extract[SensorResult].some).successNel[Throwable];
            }
          }
        }
    }
  }

  def findById(sensorID: Option[List[String]]): ValidationNel[Throwable, SensorResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Sensor", "findById:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("idList", sensorID))
    (sensorID map {
      _.map { asm_id =>
        play.api.Logger.debug(("%-20s -->[%s]").format("Sensor ID", asm_id))
        (riak.fetch(asm_id) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(asm_id, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              (Validation.fromTryCatchThrowable[SensorResult,Throwable] {
                parse(xs.value).extract[SensorResult]
              } leftMap { t: Throwable => new MalformedBodyError(xs.value, t.getMessage) }).toValidationNel.flatMap { j: SensorResult =>
                play.api.Logger.debug(("%-20s -->[%s]").format("Sensor result", j))
                Validation.success[Throwable, SensorResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ?
              }
            }
            case None => {
              Validation.failure[Throwable, SensorResults](new ResourceItemNotFound(asm_id, "")).toValidationNel
            }
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((SensorResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head.
  }

  def findByEmail(email: String): ValidationNel[Throwable, SensorResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("tosca.Sensor", "findByEmail:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Throwable, SensorResults]] {
      (((for {
        aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      } yield {
        val bindex = ""
        val bvalue = Set("")
        play.api.Logger.debug(("%-20s -->[%s]").format("tosca.Sensor", "findByEmail" + aor.get.id))
        new GunnySack("sensor", aor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
          None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
      }) leftMap { t: NonEmptyList[Throwable] => t } flatMap {
        gs: Option[GunnySack] => riak.fetchIndexByValue(gs.get)
      } map { nm: List[String] =>
        (if (!nm.isEmpty) findById(nm.some) else
          new ResourceItemNotFound(email, "Sensor = nothing found for the user.").failureNel[SensorResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    res.getOrElse(new ResourceItemNotFound(email, "Sensor = nothing found for the users.").failureNel[SensorResults])
  }



}

object SensorList {

  implicit val formats = DefaultFormats

  implicit def SensorResultsSemigroup: Semigroup[SensorResults] = Semigroup.instance((f1, f2) => f1.append(f2))

  val emptyRR = List(Sensor.empty)
  def toJValue(nres: SensorList): JValue = {

    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.tosca.SensorListSerialization.{ writer => SensorListWriter }
    toJSON(nres)(SensorListWriter)
  }

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[SensorList] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.tosca.SensorListSerialization.{ reader => SensorListReader }
    fromJSON(jValue)(SensorListReader)
  }

  def toJson(nres: SensorList, prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue(nres)))
  } else {
    compactRender(toJValue(nres))
  }

  def apply(sensorList: List[Sensor]): SensorList = { println(sensorList); sensorList }

  def empty: List[Sensor] = emptyRR

  private val riak = GWRiak("sensor")

  val metadataKey = "SENSOR"
  val metadataVal = "Sensor Creation"
  val bindex = "sensor"

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
   private def mkGunnySack(email: String, input: Sensor, asm_id: String): ValidationNel[Throwable, Option[GunnySack]] = {
     play.api.Logger.debug(("%-20s -->[%s]").format("tosca.Sensor", "mkGunnySack:Entry"))
     play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

     for {
       aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
       uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "com").get leftMap { ut: NonEmptyList[Throwable] => ut })
     } yield {

       val bvalue = Set(aor.get.id)
       val json = "{\"id\": \"" + (uir.get._1 + uir.get._2) + "\", \"Sensor_type\":\"" + input.sensor_type + "\",\"Payload\":" + PayloadList.toJson(input.payload, true) + "\",\"created_at\":\"" + Time.now.toString + "\"}"

       new GunnySack((uir.get._1 + uir.get._2), json, RiakConstants.CTYPE_TEXT_UTF8, None,
         Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
     }
  }

  def createLinks(email: String, input: SensorList, asm_id: String): ValidationNel[Throwable, SensorResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("tosca.Sensor", "createLinks:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))
    var res = (SensorResults.empty).successNel[Throwable]
    if (input.isEmpty) {
      res = (SensorResults.empty).successNel[Throwable]
    } else {
       res = (input map {
        asminp =>
         play.api.Logger.debug(("%-20s -->[%s]").format("sensor", asminp))
          (create(email, asminp, asm_id))
        }).foldRight((SensorResults.empty).successNel[Throwable])(_ +++ _)
    }
    play.api.Logger.debug(("%-20s -->[%s]").format("models.tosca.Sensor", res))
    //res.getOrElse(new ResourceItemNotFound(email, "nodes = ah. ouh. ror some reason.").failureNel[ComponentsResults])
    res
  }

  /*
   * create new market place item with the 'name' of the item provide as input.
   * A index name assemblies name will point to the "csars" bucket
   */
  def create(email: String, input: Sensor, asm_id: String): ValidationNel[Throwable, SensorResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("tosca.Sensor", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(email, input, asm_id) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input.sensor_type, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>

      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => nels((parse(thatGS.value).extract[SensorResult]).some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("Sensor.created success", "Scaliak returned => None. Thats OK."))
              nels((parse(gs.get.value).extract[SensorResult]).some).successNel[Throwable];
            }
          }
        }
    }


  }
}
