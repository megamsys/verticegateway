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
package models

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
//import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps
import org.megam.util.Time
import Scalaz._
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


case class SshKeyInput(name: String, path: String) {
  val json = "{\"name\":\"" + name + "\",\"path\":\"" + path + "\"}"
}

case class SshKeyResult(id: String, name: String, accounts_id: String, path: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.SshKeyResultSerialization
    val preser = new SshKeyResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object SshKeyResult {
  

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[SshKeyResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.SshKeyResultSerialization
    val preser = new SshKeyResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[SshKeyResult] = (Validation.fromTryCatch[net.liftweb.json.JValue] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

   
}

object SshKeys {

  implicit val formats = DefaultFormats
  private val riak = GWRiak("sshkeys")
  implicit def SshKeyResultsSemigroup: Semigroup[SshKeyResults] = Semigroup.instance((f1, f2) => f1.append(f2))

  val metadataKey = "SshKey"
  val metadataVal = "SshKeys Creation"
  val bindex = "sshkey"

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.SshKeys", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val sshKeyInput: ValidationNel[Throwable, SshKeyInput] = (Validation.fromTryCatch[models.SshKeyInput] {
      parse(input).extract[SshKeyInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      pdc <- sshKeyInput
      //TO-DO: Does the leftMap make sense ? To check during function testing, by removing it.
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "ssh").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for None on aor, and uir (confirm it during function testing).
      val bvalue = Set(aor.get.id)
      val json = new SshKeyResult(uir.get._1 + uir.get._2, pdc.name, aor.get.id, pdc.path, Time.now.toString).toJson(false)
      new GunnySack(pdc.name, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new Node with the 'name' of the node provide as input.
   * A index name accountID will point to the "accounts" bucket
   */
  def create(email: String, input: String): ValidationNel[Throwable, Option[SshKeyResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.SshKeys", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[SshKeyResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("SshKey.created success", "Scaliak returned => None. Thats OK."))
              (parse(gs.get.value).extract[SshKeyResult].some).successNel[Throwable];
            }
          }

        }
    }
  }

  def findByName(sshKeysNameList: Option[List[String]]): ValidationNel[Throwable, SshKeyResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.SshKeys", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("SshKeysList", sshKeysNameList))
    (sshKeysNameList map {
      _.map { sshKeysName =>
        play.api.Logger.debug("models.SshKeysName findByName: SshKeys:" + sshKeysName)
        (riak.fetch(sshKeysName) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(sshKeysName, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              (Validation.fromTryCatch[models.SshKeyResult] {
                parse(xs.value).extract[SshKeyResult]
              } leftMap { t: Throwable =>
                new ResourceItemNotFound(sshKeysName, t.getMessage)
              }).toValidationNel.flatMap { j: SshKeyResult =>
                Validation.success[Throwable, SshKeyResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ? 
              }
            }
            case None => Validation.failure[Throwable, SshKeyResults](new ResourceItemNotFound(sshKeysName, "")).toValidationNel
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((SshKeyResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head. 

  }

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email, 
   * the nodenames are listed on the index (account.id) in bucket `Nodes`.
   * Using a "nodename" as key, return a list of ValidationNel[List[NodeResult]] 
   * Takes an email, and returns a Future[ValidationNel, List[Option[NodeResult]]]
   */
  def findByEmail(email: String): ValidationNel[Throwable, SshKeyResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.SshKeys", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Throwable, SshKeyResults]] {
      (((for {
        aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      } yield {
        val bindex = ""
        val bvalue = Set("")
        new GunnySack("sshkey", aor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
          None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
      }) leftMap { t: NonEmptyList[Throwable] => t } flatMap {
        gs: Option[GunnySack] => riak.fetchIndexByValue(gs.get)
      } map { nm: List[String] =>
        (if (!nm.isEmpty) findByName(nm.some) else
          new ResourceItemNotFound(email, "SshKeys = nothing found.").failureNel[SshKeyResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    res.getOrElse(new ResourceItemNotFound(email, "SshKeys = nothing found.").failureNel[SshKeyResults])
  }

}