/* 
** Copyright [2012-2014] [Megam Systems]
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
import scalaz.syntax.SemigroupOps
import scalaz.NonEmptyList._
import scalaz.Validation._
import scalaz.effect.IO
import scalaz.EitherT._
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
 * @author morpheyesh
 *
 */

case class OrganizationsInput(name: String) {
  val json = "{\"name\":\"" + name + "\"}"
}

case class OrganizationsResult(id: String, name: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.tosca.OrganizationsResultSerialization
    val preser = new OrganizationsResultSerialization()
    toJSON(this)(preser.writer) //where does this JSON from? 
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object OrganizationsResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[OrganizationsResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.tosca.OrganizationsResultSerialization
    val preser = new OrganizationsResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[OrganizationsResult] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Organizations {
  implicit val formats = DefaultFormats
  private val riak = GWRiak("organizations")

  implicit def OrganizationsResultsSemigroup: Semigroup[OrganizationsResults] = Semigroup.instance((f1, f2) => f1.append(f2))
  //implicit def OrganizationsProcessedResultsSemigroup: Semigroup[NodeProcessedResults] = Semigroup.instance((f3, f4) => f3.append(f4))

  
  
  val metadataKey = "Organizations"
  val metadataVal = "Organizations Creation"
  val bindex = "organization"

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to organizationsinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.tosca.Organizations", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val organizationsInput: ValidationNel[Throwable, OrganizationsInput] = (Validation.fromTryCatch {
      parse(input).extract[OrganizationsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      org <- organizationsInput
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "org").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(org.name)
      val json = new OrganizationsResult(uir.get._1 + uir.get._2, org.name, Time.now.toString).toJson(false)
      new GunnySack(org.name, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new organization item with the 'name' of the item provide as input.
   * A index name organization name will point to the "organization bucket" bucket.
   */

  def create(email: String, input: String): ValidationNel[Throwable, Option[OrganizationsResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Organizations", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[OrganizationsResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("Organizations.created success", "Scaliak returned => None. Thats OK."))
              (parse(gs.get.value).extract[OrganizationsResult].some).successNel[Throwable];
            }
          }
        }
    }
  }

  def findByName(organizationsList: Option[List[String]]): ValidationNel[Throwable, OrganizationsResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Organizations", "findByName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("nodeNameList", organizationsList))
    (organizationsList map {
      _.map { organizationsName =>
        play.api.Logger.debug(("%-20s -->[%s]").format("organizationsName", organizationsName))
        (riak.fetch(organizationsName) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(organizationsName, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              //JsonScalaz.Error doesn't descend from java.lang.Error or Throwable. Screwy.
              (OrganizationsResult.fromJson(xs.value) leftMap
                { t: NonEmptyList[net.liftweb.json.scalaz.JsonScalaz.Error] =>
                  JSONParsingError(t)
                }).toValidationNel.flatMap { j: OrganizationsResult =>
                  play.api.Logger.debug(("%-20s -->[%s]").format("organizationsresult", j))
                  Validation.success[Throwable, OrganizationsResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ? 
                }
            }
            case None => {
              Validation.failure[Throwable, OrganizationsResults](new ResourceItemNotFound(organizationsName, "")).toValidationNel
            }
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((OrganizationsResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head. 
  }
}

