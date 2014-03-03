/* 
** Copyright [2012-2013] [Megam Systems]
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
import scalaz.syntax.SemigroupOps
import scalaz.NonEmptyList._
import scalaz.Validation._
import scalaz.effect.IO
import scalaz.EitherT._
import com.twitter.util.Time
import Scalaz._
import controllers.stack._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import models._
import com.stackmob.scaliak._
import com.basho.riak.client.query.indexes.{ RiakIndexes, IntIndex, BinIndex }
import com.basho.riak.client.http.util.{ Constants => RiakConstants }
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author rajthilak
 *
 */

case class PredefCloudSpec(type_name: String, groups: String, image: String, flavor: String, tenant_id: String) {
  val json = "\"type_name\":\"" + type_name + "\",\"groups\":\"" + groups + "\",\"image\":\"" + image + "\",\"flavor\":\"" + flavor + "\",\"tenant_id\":\"" + tenant_id + "\""
}

case class PredefCloudAccess(ssh_key: String, identity_file: String, ssh_user: String, vault_location: String, sshpub_location: String, zone: String, region: String) {
  val json = "\"ssh_key\":\"" + ssh_key + "\",\"identity_file\":\"" + identity_file + "\",\"ssh_user\":\"" + ssh_user + "\",\"vault_location\":\"" + vault_location + "\",\"sshpub_location\":\"" + sshpub_location + "\",\"zone\":\"" + zone + "\",\"region\":\"" + region + "\""            
}

case class PredefCloudInput(name: String, spec: PredefCloudSpec, access: PredefCloudAccess) {
  val json = "{\"name\":\"" + name + "\",\"spec\":{" + spec.json + "},\"access\":{" + access.json + "}" + "}"
}

case class PredefCloudResult(id: String, name: String, accounts_id: String, spec: PredefCloudSpec, access: PredefCloudAccess,
  ideal: String = new String(), performance: String = new String(), created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.PredefCloudResultSerialization
    val preser = new PredefCloudResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object PredefCloudResult {
  

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[PredefCloudResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.PredefCloudResultSerialization
    val preser = new PredefCloudResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[PredefCloudResult] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

   
}

object PredefClouds {

  implicit val formats = DefaultFormats
  private def riak: GSRiak = GSRiak(MConfig.riakurl, "predefclouds")
  implicit def PredefCloudResultsSemigroup: Semigroup[PredefCloudResults] = Semigroup.instance((f1, f2) => f1.append(f2))

  val metadataKey = "Predefcloud"
  val metadataVal = "Predefs Creation"
  val bindex = BinIndex.named("predefcloud")

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.PredefClouds", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val predefCloudInput: ValidationNel[Throwable, PredefCloudInput] = (Validation.fromTryCatch {
      parse(input).extract[PredefCloudInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      pdc <- predefCloudInput
      //TO-DO: Does the leftMap make sense ? To check during function testing, by removing it.
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "pdc").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for None on aor, and uir (confirm it during function testing).
      val bvalue = Set(aor.get.id)
      val json = new PredefCloudResult(uir.get._1 + uir.get._2, pdc.name+uir.get._2, aor.get.id, pdc.spec, pdc.access, "", "", Time.now.toString).toJson(false)
      new GunnySack(pdc.name+uir.get._2, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new Node with the 'name' of the node provide as input.
   * A index name accountID will point to the "accounts" bucket
   */
  def create(email: String, input: String): ValidationNel[Throwable, Option[PredefCloudResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.PredefClouds", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[PredefCloudResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("PredefsCloud.created success", "Scaliak returned => None. Thats OK."))
              (parse(gs.get.value).extract[PredefCloudResult].some).successNel[Throwable];
            }
          }

        }
    }
  }

  def findByName(predefCloudsNameList: Option[List[String]]): ValidationNel[Throwable, PredefCloudResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.PredefClouds", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("predefCloudsList", predefCloudsNameList))
    (predefCloudsNameList map {
      _.map { predefcloudsName =>
        play.api.Logger.debug("models.PredefCloudsName findByName: predefclouds:" + predefcloudsName)
        (riak.fetch(predefcloudsName) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(predefcloudsName, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              (Validation.fromTryCatch {
                parse(xs.value).extract[PredefCloudResult]
              } leftMap { t: Throwable =>
                new ResourceItemNotFound(predefcloudsName, t.getMessage)
              }).toValidationNel.flatMap { j: PredefCloudResult =>
                Validation.success[Throwable, PredefCloudResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ? 
              }
            }
            case None => Validation.failure[Throwable, PredefCloudResults](new ResourceItemNotFound(predefcloudsName, "")).toValidationNel
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((PredefCloudResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head. 

  }

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email, 
   * the nodenames are listed on the index (account.id) in bucket `Nodes`.
   * Using a "nodename" as key, return a list of ValidationNel[List[NodeResult]] 
   * Takes an email, and returns a Future[ValidationNel, List[Option[NodeResult]]]
   */
  def findByEmail(email: String): ValidationNel[Throwable, PredefCloudResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.PredefClouds", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Throwable, PredefCloudResults]] {
      (((for {
        aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      } yield {
        val bindex = BinIndex.named("")
        val bvalue = Set("")
        new GunnySack("predefcloud", aor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
          None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
      }) leftMap { t: NonEmptyList[Throwable] => t } flatMap {
        gs: Option[GunnySack] => riak.fetchIndexByValue(gs.get)
      } map { nm: List[String] =>
        (if (!nm.isEmpty) findByName(nm.some) else
          new ResourceItemNotFound(email, "predefclouds = nothing found.").failureNel[PredefCloudResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    res.getOrElse(new ResourceItemNotFound(email, "predefclouds = nothing found.").failureNel[PredefCloudResults])
  }

}