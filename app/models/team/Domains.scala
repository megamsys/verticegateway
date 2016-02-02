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
package models.team

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import cache._
import db._
import models.json.team._
import controllers.Constants._
import io.megam.auth.funnel.FunnelErrors._
import app.MConfig

import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import io.megam.common.riak.GunnySack
import io.megam.common.uid.UID
import io.megam.util.Time
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author morpheyesh
 *
 */

case class DomainsInput(name: String) {
  val json = "{\"name\":\"" + name + "\"}"
}

case class DomainsResult(id: String, name: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.team.DomainsResultSerialization
    val preser = new DomainsResultSerialization()
    toJSON(this)(preser.writer) //where does this JSON from?
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object DomainsResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[DomainsResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.team.DomainsResultSerialization
    val preser = new DomainsResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[DomainsResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Domains {
  implicit val formats = DefaultFormats
  private val riak = GWRiak("domains")
  implicit def DomainsResultsSemigroup: Semigroup[DomainsResults] = Semigroup.instance((f1, f2) => f1.append(f2))
  val metadataKey = "Domains"
  val metadataVal = "Domains Creation"
  val bindex = "domains"

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to domainsinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    val domainsInput: ValidationNel[Throwable, DomainsInput] = (Validation.fromTryCatchThrowable[DomainsInput, Throwable] {
      parse(input).extract[DomainsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      domain <- domainsInput
      uir <- (UID("dom").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(domain.name)
      val json = new DomainsResult(uir.get._1 + uir.get._2, domain.name, Time.now.toString).toJson(false)
      new GunnySack(domain.name, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new organization item with the 'name' of the item provide as input.
   * A index name organization name will point to the "organization bucket" bucket.
   */

  def create(email: String, input: String): ValidationNel[Throwable, Option[DomainsResult]] = {
    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[DomainsResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Domains.created success", Console.RESET))
              (parse(gs.get.value).extract[DomainsResult].some).successNel[Throwable];
            }
          }
        }
    }
  }

  def findByName(domainsList: Option[List[String]]): ValidationNel[Throwable, DomainsResults] = {
    (domainsList map {
      _.map { domainsName =>
        play.api.Logger.debug(("%-20s -->[%s]").format("domainsName", domainsName))
        (riak.fetch(domainsName) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(domainsName, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              //JsonScalaz.Error doesn't descend from java.lang.Error or Throwable. Screwy.
              (DomainsResult.fromJson(xs.value) leftMap
                { t: NonEmptyList[net.liftweb.json.scalaz.JsonScalaz.Error] =>
                  JSONParsingError(t)
                }).toValidationNel.flatMap { j: DomainsResult =>
                  play.api.Logger.debug(("%-20s -->[%s]").format("domainsresult", j))
                  Validation.success[Throwable, DomainsResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ?
                }
            }
            case None => {
              Validation.failure[Throwable, DomainsResults](new ResourceItemNotFound(domainsName, "")).toValidationNel
            }
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((DomainsResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head.
  }

}
