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
package models.base

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import models.base._
import db._
import cache._
import app.MConfig
import app.MConfig
import controllers.Constants._
import controllers.funnel.FunnelErrors._


import org.megam.common.amqp.response.AMQPResponse
import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import org.megam.common.riak.GunnySack
import org.megam.util.Time
import org.megam.common.uid._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz.{ Result, UncategorizedError }
import java.nio.charset.Charset

/**
 * @author ram
 */
case class RequestInput(cat_id: String,
  cattype: String,
  name: String,
  action: String,
  category: String) {
  val half_json = "\"cat_id\":\"" + cat_id + "\",\"cattype\":\"" + cattype + "\",\"name\":\"" + name + "\",\"action\":\"" + action + "\",\"category\":\"" + category + "\""

  val json = "{"+half_json +"}"
}

case class RequestResult(id: String, cat_id: String, cattype: String, name: String, action: String, category: String, created_at: String) {
  val json = "{\"id\": \"" + id + "\",\"cat_id\": \"" + cat_id + "\",\"cattype\":\"" + cattype + "\",\"name\":\"" + name + "\",\"action\":\"" + action + "\",\"category\":\"" + category + "\",\"created_at\":\"" + created_at + "\"}"

  def toMap: Map[String, String] = {
    Map[String, String](
      ("id" -> id),
      ("cat_id" -> cat_id),
      ("cat_type" -> cattype),
      ("name" -> name),
      ("action" -> action),
      ("category" -> category))
  }

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.RequestResultSerialization
    val nrsser = new RequestResultSerialization()
    toJSON(this)(nrsser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object RequestResult {

  def apply = new RequestResult(new String(), new String(), new String(), new String(), new String(), new String(), new String)

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[RequestResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.RequestResultSerialization
    val nrsser = new RequestResultSerialization()
    fromJSON(jValue)(nrsser.reader)
  }

  def fromJson(json: String): Result[RequestResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Requests {

  implicit val formats = DefaultFormats

  implicit def RequestResultsSemigroup: Semigroup[RequestResults] = Semigroup.instance((f1, f2) => f1.append(f2))

  private val riak = GWRiak("requests")

  val metadataKey = "Request"
  val newreq_metadataVal = "New Request"
  val newAMS_bindex = "amsId"

  // A private method which chains computation to make GunnySack parses the json, and converts it to requestinput, if there is an error during parsing, a MalformedBodyError is sent back.
  // After that flatMap on its success and the GunnySack object is built.
  private def mkGunnySack(input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    val ripNel: ValidationNel[Throwable, RequestInput] = (Validation.fromTryCatchThrowable[models.base.RequestInput, Throwable] {
      parse(input).extract[RequestInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
    for {
      rip <- ripNel
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "rip").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(rip.cat_id)
      val json = "{\"id\": \"" + (uir.get._1 + uir.get._2) + "\"," + rip.half_json + ",\"created_at\":\"" + Time.now.toString + "\"}"
      new GunnySack((uir.get._1 + uir.get._2), json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> newreq_metadataVal), Map((newAMS_bindex, bvalue))).some
    }
  }

  //create request from input
  def create(input: String): ValidationNel[Throwable, Option[wash.PQd]] = {
    for {
      ogsi <- mkGunnySack(input) leftMap { err: NonEmptyList[Throwable] => err }
      nrip <- RequestResult.fromJson(ogsi.get.value) leftMap { t: NonEmptyList[net.liftweb.json.scalaz.JsonScalaz.Error] => nels(JSONParsingError(t)) }
      ogsr <- riak.store(ogsi.get) leftMap { t: NonEmptyList[Throwable] => t }
    } yield {
      ogsr match {
        case Some(thatGS) => {
          new wash.PQd(nrip).some
        }
        case None => {
          play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Request.created successfully", Console.RESET))
          new wash.PQd(nrip).some
        }
      }
    }

  }

  // create a request and publish
  def createAndPub(email: String, input: String): ValidationNel[Throwable, Option[wash.PQd]] = {
    (create(input) leftMap { err: NonEmptyList[Throwable] =>
      err
    }).flatMap { pq: Option[wash.PQd] =>
      if(!email.equalsIgnoreCase(controllers.Constants.DEMO_EMAIL)) {
      (new wash.AOneWasher(pq.get).wash leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: AMQPResponse =>
            play.api.Logger.debug(("%-20s -->[%s]").format("Request.published successfully", input))
            pq.successNel[Throwable]
      }
    } else {
        play.api.Logger.debug(("%-20s -->[%s]").format("Request.publish skipped", input))
        wash.PQd.empty.some.successNel[Throwable]
      }
    }
  }
  /**
   * List all the requests for the requestlist.
   */
  def findByReqName(reqNameList: Option[List[String]]): ValidationNel[Error, RequestResults] = {
    (reqNameList map {
      _.map { reqName =>
        (riak.fetch(reqName) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(reqName, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              (Validation.fromTryCatchThrowable[models.base.RequestResult, Throwable] {
                parse(xs.value).extract[RequestResult]
              } leftMap { t: Throwable =>
                new ResourceItemNotFound(reqName, t.getMessage)
              }).toValidationNel.flatMap { j: RequestResult =>
                Validation.success[Error, RequestResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ?
              }
            }
            case None => Validation.failure[Error, RequestResults](new ResourceItemNotFound(reqName, "")).toValidationNel
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((RequestResults.empty).successNel[Error])(_ +++ _)
    }).head //return the folded element in the head.
  }

}
