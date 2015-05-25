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
import scalaz.syntax.SemigroupOps
import org.megam.util.Time
import Scalaz._
import controllers.stack._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import models.tosca._
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
 * @author rajthilak
 *
 */

case class ContiniousIntegrationInput(enable: String, scm: String, token: String, owner: String, component_id: String, assembly_id: String) {
  val json = "{\"enable\":\"" + enable + "\",\"scm\":\"" + scm + "\",\"token\":\"" + token + "\",\"owner\":\"" + owner + "\",\"component_id\":\"" + component_id + "\",\"assembly_id\":\"" + assembly_id + "\"}"
}

case class ContiniousIntegrationResult(id: String, enable: String, scm: String, token: String, owner: String, component_id: String, assembly_id: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.tosca.ContiniousIntegrationResultSerialization
    val preser = new ContiniousIntegrationResultSerialization()
    toJSON(this)(preser.writer) //where does this JSON from?
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object ContiniousIntegrationResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[ContiniousIntegrationResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.tosca.ContiniousIntegrationResultSerialization
    val preser = new ContiniousIntegrationResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[ContiniousIntegrationResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue,Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object ContiniousIntegration {
  implicit val formats = DefaultFormats
  private val riak = GWRiak("cig")

  implicit def ContiniousIntegrationResultsSemigroup: Semigroup[ContiniousIntegrationResults] = Semigroup.instance((f1, f2) => f1.append(f2))
  //implicit def DomainsProcessedResultsSemigroup: Semigroup[NodeProcessedResults] = Semigroup.instance((f3, f4) => f3.append(f4))

  val metadataKey = "ContiniousIntegration"
  val metadataVal = "ContiniousIntegration Creation"
  val bindex = "cig"

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to domainsinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.tosca.ContiniousIntegration", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val cigInput: ValidationNel[Throwable, ContiniousIntegrationInput] = (Validation.fromTryCatchThrowable[ContiniousIntegrationInput, Throwable] {
      parse(input).extract[ContiniousIntegrationInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      cig <- cigInput
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "cig").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(cig.scm)
      val json = new ContiniousIntegrationResult(uir.get._1 + uir.get._2, cig.enable, cig.scm, cig.token, cig.owner, cig.component_id, cig.assembly_id, Time.now.toString).toJson(false)
      new GunnySack((uir.get._1 + uir.get._2), json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new organization item with the 'name' of the item provide as input.
   * A index name organization name will point to the "organization bucket" bucket.
   */

  def create(email: String, input: String): ValidationNel[Throwable, Option[Tuple2[String, String]]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.ContiniousIntegration", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => Tuple2(thatGS.key, "").some.successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("ContiniousIntegration.created success", "Scaliak returned => None. Thats OK."))
              (gs.get.key, "").some.successNel[Throwable];
            }
          }
        }
    }
  }


 /* def findByName(domainsList: Option[List[String]]): ValidationNel[Throwable, DomainsResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Domains", "findByName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("domainsList", domainsList))
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
  */

}
