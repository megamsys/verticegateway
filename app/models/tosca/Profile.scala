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

case class ProfileInput(first_name: String, last_name: String, email: String, api_token: String, password: String, password_confirmation: String, password_reset_token: String ) {
  val json = "{\"first_name\":\"" + first_name + "\",\"last_name\":\"" + last_name + "\",\"email\":" + email + "\",\"api_token\":\"" + api_token + "\",\"password\":\"" + password + "\",\"password_confirmation\":\"" + password_confirmation + "\",\"password_reset_token\":\"" + password_reset_token + "\"}"
}

case class ProfileResult(id: String, accounts_id: String, first_name: String, last_name: String, email: String, api_token: String, password: String, password_confirmation: String, password_reset_token: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.tosca.ProfileResultSerialization
    val preser = new ProfileResultSerialization()
    toJSON(this)(preser.writer) //where does this JSON from? 
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object ProfileResult {


   //def apply(id: String, accounts_id: String, first_name: String, last_name: String, email: String, api_token: String, password: String, password_confirmation: String, password_reset_token: String, created_at: String) = new ProfileResult(id, accounts_id, first_name, last_name, email, api_token, password, password_confirmation, password_reset_token, created_at)

  
   def apply(email: String): ProfileResult = ProfileResult("not found", new String(), new String(), new String(), email, new String(), new String(),new String(), new String(), new String())

  
  
  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[ProfileResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.tosca.ProfileResultSerialization
    val preser = new ProfileResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[ProfileResult] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Profile {
  implicit val formats = DefaultFormats
  private val riak = GWRiak("profile")

  //implicit def ProfileResultsSemigroup: Semigroup[ProfileResults] = Semigroup.instance((f1, f2) => f1.append(f2))
  
  
  val metadataKey = "Profile"
  val metadataVal = "Profile"
  val bindex = "profile"

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to profile input, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.tosca.Profile", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val profileInput: ValidationNel[Throwable, ProfileInput] = (Validation.fromTryCatch {
      parse(input).extract[ProfileInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      profile <- profileInput
      aor <- (models.Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      bal <- (models.billing.Balances.create(email, "{\"credit\":\"0\"}") leftMap { t: NonEmptyList[Throwable] => t })
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "profile").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(aor.get.id)
      val json = new ProfileResult(uir.get._1 + uir.get._2, aor.get.id, profile.first_name, profile.last_name, profile.email, profile.api_token, profile.password, profile.password_confirmation, profile.password_reset_token, Time.now.toString).toJson(false)
      new GunnySack(profile.email, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new profile item with the 'name' of the item provide as input.
   * A index name profile name will point to the "profile bucket" bucket.
   */

  def create(email: String, input: String): ValidationNel[Throwable, Option[ProfileResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Profile", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[ProfileResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("Profile.created success", "Scaliak returned => None. Thats OK."))
              (parse(gs.get.value).extract[ProfileResult].some).successNel[Throwable];
            }
          }
        }
    }
  }

  
  
  def findByEmail(email: String): ValidationNel[Throwable, Option[ProfileResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Profile", "findByEmail:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("findByEmail", email))
    InMemory[ValidationNel[Throwable, Option[ProfileResult]]]({
      name: String =>
        {
          play.api.Logger.debug(("%-20s -->[%s]").format("InMemory", email))
          (riak.fetch(email) leftMap { t: NonEmptyList[Throwable] =>
            new ServiceUnavailableError(email, (t.list.map(m => m.getMessage)).mkString("\n"))
          }).toValidationNel.flatMap { xso: Option[GunnySack] =>
            xso match {
              case Some(xs) => {
                (Validation.fromTryCatch[models.tosca.ProfileResult] {
                //  initiate_default_cloud(email)
                  parse(xs.value).extract[ProfileResult]
                } leftMap { t: Throwable =>
                  new ResourceItemNotFound(email, t.getMessage)
                }).toValidationNel.flatMap { j: ProfileResult =>
                  Validation.success[Throwable, Option[ProfileResult]](j.some).toValidationNel
                }
              }
              case None => Validation.failure[Throwable, Option[ProfileResult]](new ResourceItemNotFound(email, "")).toValidationNel
            }
          }
        }
    }).get(email).eval(InMemoryCache[ValidationNel[Throwable, Option[ProfileResult]]]())

  }
  
  implicit val sedimentProfileEmail = new Sedimenter[ValidationNel[Throwable, Option[ProfileResult]]] {
    def sediment(maybeASediment: ValidationNel[Throwable, Option[ProfileResult]]): Boolean = {
      val notSed = maybeASediment.isSuccess
      play.api.Logger.debug("%-20s -->[%s]".format("|^/^|-->ACT:sediment:", notSed))
      notSed
    }
  }

  
  
}

