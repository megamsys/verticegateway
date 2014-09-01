/* 
** Copyright [2013-2014] [Megam Systems]
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
import scalaz.effect.IO
import scalaz.Validation._
import scalaz.EitherT._
import Scalaz._
import org.megam.util.Time
import scala.collection.mutable.ListBuffer
import controllers.stack._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import models.tosca._
import models._
import models.riak._
import models.cache._
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
case class AssembliesInput(name: String, assemblies: models.tosca.AssembliesList, inputs: AssembliesInputs) {
  val json = "{\"name\":\"" + name + "\", \"assemblies\":" + AssembliesList.toJson(assemblies, true) + ",\"inputs\":" + inputs.json + "}"
}

case class AssembliesInputs(id: String, assemblies_type: String, label: String) {
  val json = "{\"id\": \"" + id + "\", \"assemblies_type\":\"" + assemblies_type + "\",\"label\" : \"" + label + "\"}"
}

case class AssembliesResult(id: String, name: String, assemblies: models.tosca.AssemblyLinks, inputs: AssembliesInputs, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.tosca.AssembliesResultSerialization
    val preser = new AssembliesResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object AssembliesResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[AssembliesResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.tosca.AssembliesResultSerialization
    val preser = new AssembliesResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[AssembliesResult] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Assemblies {

  implicit val formats = DefaultFormats
  private val riak = GWRiak("assemblies")

  val metadataKey = "ASSEMBLIES"
  val metadataVal = "Assemblies Creation"
  val bindex = "assemblies"

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("tosca.Assemblies", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val ripNel: ValidationNel[Throwable, AssembliesInput] = (Validation.fromTryCatch {
      parse(input).extract[AssembliesInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      rip <- ripNel
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      aem <- (AssembliesList.createLinks(email, rip.assemblies) leftMap { t: NonEmptyList[Throwable] => t })
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "ams").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(aor.get.id)
      var assembly_links = new ListBuffer[String]()
      for (assembly <- aem) {
        assembly match {
          case Some(value) => assembly_links += value.id
          case None        => assembly_links += ""
        }
      }
      val json = new AssembliesResult(uir.get._1 + uir.get._2, rip.name, assembly_links.toList, rip.inputs, Time.now.toString).toJson(false)
      new GunnySack((uir.get._1 + uir.get._2), json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new market place item with the 'name' of the item provide as input.
   * A index name assemblies name will point to the "csars" bucket
   */
  def create(email: String, input: String): ValidationNel[Throwable, Option[AssembliesResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("tosca.Assemblies", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("yaml", input))

    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>

      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[AssembliesResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("Assemblies.created success", "Scaliak returned => None. Thats OK."))
              (parse(gs.get.value).extract[AssembliesResult].some).successNel[Throwable];
            }
          }
        }
    }
  }

}