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
package models

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps
import org.megam.util.Time
import controllers.stack._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import models._
import models.riak._
import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{RiakIndexes, StringBinIndex, LongIntIndex }
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

case class CloudToolSettingInput(cloud_type: String, repo_name: String, repo: String, vault_location: String, conf_location: String) {
  val json = "{\"cloud_type\":\"" + cloud_type + "\",\"repo_name\":\"" + repo_name + "\",\"repo\":\"" + repo + "\",\"vault_location\":\"" + vault_location + "\",\"conf_location\":\"" + conf_location + "\"}"
}

case class CloudToolSettingResult(id: String, accounts_id: String, cloud_type: String, repo_name: String, repo: String, vault_location: String, conf_location: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.CloudToolSettingResultSerialization
    val preser = new CloudToolSettingResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object CloudToolSettingResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[CloudToolSettingResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.CloudToolSettingResultSerialization
    val preser = new CloudToolSettingResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[CloudToolSettingResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue,Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object CloudToolSettings {

  implicit val formats = DefaultFormats
  private val riak = GWRiak( "CloudToolSettings")
  implicit def CloudToolSettingResultsSemigroup: Semigroup[CloudToolSettingResults] = Semigroup.instance((f1, f2) => f1.append(f2))

  val metadataKey = "CloudToolSetting"
  val metadataVal = "CloudToolSetting Creation"
  val bindex = "CloudToolSetting"

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to CloudToolSetting input, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.CloudToolSettings", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val CloudToolSettingInput: ValidationNel[Throwable, CloudToolSettingInput] = (Validation.fromTryCatchThrowable[models.CloudToolSettingInput,Throwable] {
      parse(input).extract[CloudToolSettingInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      pdc <- CloudToolSettingInput
      //TO-DO: Does the leftMap make sense ? To check during function testing, by removing it.
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "cts").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for None on aor, and uir (confirm it during function testing).
      val bvalue = Set(aor.get.id)
      val json = new CloudToolSettingResult(uir.get._1 + uir.get._2, aor.get.id, pdc.cloud_type, pdc.repo_name, pdc.repo, pdc.vault_location, pdc.conf_location, Time.now.toString).toJson(false)
      new GunnySack(uir.get._1 + uir.get._2, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new Node with the 'name' of the node provide as input.
   * A index name CloudToolSettingID will point to the "CloudToolSettings" bucket
   */
  def create(email: String, input: String): ValidationNel[Throwable, Option[Tuple3[String, String, String]]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.CloudToolSettings", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      val req_result = parse(gs.get.value).extract[CloudToolSettingResult]
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => Tuple3(thatGS.key, req_result.vault_location, req_result.repo).some.successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("CloudToolSettings.created success", "Scaliak returned => None. Thats OK."))
              (gs.get.key, req_result.vault_location, req_result.repo).some.successNel[Throwable];
            }
          }

        }
    }
  }

  def findByName(cloudtoolsettingList: Option[List[String]]): ValidationNel[Throwable, CloudToolSettingResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.CloudToolSettings", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("CloudToolSettingsList", cloudtoolsettingList))
    (cloudtoolsettingList map {
      _.map { CloudToolSettingName =>
        play.api.Logger.debug("models.CloudToolSetting findByName: CloudToolSettings:" + CloudToolSettingName)
        (riak.fetch(CloudToolSettingName) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(CloudToolSettingName, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              (Validation.fromTryCatchThrowable[models.CloudToolSettingResult,Throwable] {
                parse(xs.value).extract[CloudToolSettingResult]
              } leftMap { t: Throwable =>
                new ResourceItemNotFound(CloudToolSettingName, t.getMessage)
              }).toValidationNel.flatMap { j: CloudToolSettingResult =>
                Validation.success[Throwable, CloudToolSettingResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ? 
              }
            }
            case None => Validation.failure[Throwable, CloudToolSettingResults](new ResourceItemNotFound(CloudToolSettingName, "")).toValidationNel
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((CloudToolSettingResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head. 

  }

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email, 
   * the nodenames are listed on the index (account.id) in bucket `Nodes`.
   * Using a "nodename" as key, return a list of ValidationNel[List[NodeResult]] 
   * Takes an email, and returns a Future[ValidationNel, List[Option[NodeResult]]]
   */
  def findByEmail(email: String): ValidationNel[Throwable, CloudToolSettingResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.CloudToolSettings", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Throwable, CloudToolSettingResults]] {
      (((for {
        aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      } yield {
        val bindex = ""
        val bvalue = Set("")
        new GunnySack("CloudToolSetting", aor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
          None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
      }) leftMap { t: NonEmptyList[Throwable] => t } flatMap {
        gs: Option[GunnySack] => riak.fetchIndexByValue(gs.get)
      } map { nm: List[String] =>
        (if (!nm.isEmpty) findByName(nm.some) else
          new ResourceItemNotFound(email, "CloudToolSettings = nothing found.").failureNel[CloudToolSettingResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    res.getOrElse(new ResourceItemNotFound(email, "CloudToolSettings = nothing found.").failureNel[CloudToolSettingResults])
  }

}