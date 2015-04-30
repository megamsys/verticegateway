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

package models.billing

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
import models._
import models.billing._
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

case class BalancesInput(credit: String) {
  val json = "{\"credit\":\"" + credit + "\"}"

}

case class BalancesUpdateInput(id: String, name: String, credit: String, created_at: String, updated_at: String) {
  val json = "{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"credit\":\"" + credit + "\",\"created_at\":\"" + created_at + "\",\"updated_at\":\"" + updated_at + "\"}"

}

case class BalancesResult(id: String, name: String, credit: String, created_at: String, updated_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.billing.BalancesResultSerialization
    val preser = new BalancesResultSerialization()
    toJSON(this)(preser.writer) //where does this JSON from? 
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object BalancesResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[BalancesResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.billing.BalancesResultSerialization
    val preser = new BalancesResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[BalancesResult] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Balances {
  implicit val formats = DefaultFormats
  private val riak = GWRiak("balances")

  //implicit def EventsResultsSemigroup: Semigroup[EventsResults] = Semigroup.instance((f1, f2) => f1.append(f2))
  
  
  val metadataKey = "Balances"
  val metadataVal = "Balances Creation"
  val bindex = "balances"

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to eventsinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.billing.Balances", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val balancesInput: ValidationNel[Throwable, BalancesInput] = (Validation.fromTryCatch {
      parse(input).extract[BalancesInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      balance <- balancesInput
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "bal").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(email)
      val json = new BalancesResult(uir.get._1 + uir.get._2, email, balance.credit, Time.now.toString, Time.now.toString).toJson(false)
      new GunnySack(email, json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create a new balance entry for the user.
   * 
   */

  def create(email: String, input: String): ValidationNel[Throwable, Option[BalancesResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Balances", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => (parse(thatGS.value).extract[BalancesResult].some).successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("Balances created. success", "Scaliak returned => None. Thats OK."))
              (parse(gs.get.value).extract[BalancesResult].some).successNel[Throwable];
            }
          }
        }
    }
  }
  
   private def updateGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("billing.Balance Update", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val ripNel: ValidationNel[Throwable, BalancesUpdateInput] = (Validation.fromTryCatch {
      parse(input).extract[BalancesUpdateInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      rip <- ripNel
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      val bvalue = Set(aor.get.id)

      val json = BalancesResult(rip.id, rip.name, rip.credit, rip.created_at, Time.now.toString).toJson(false)
      new GunnySack((email), json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  def update(email: String, input: String): ValidationNel[Throwable, Option[BalancesResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Balances", "update:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))
    for {
      gs <- (updateGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      maybeGS <- (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      val nrip = parse(gs.get.value).extract[BalancesResult]
      maybeGS match {
        case Some(thatGS) =>
          BalancesResult(thatGS.key, nrip.name, nrip.credit, nrip.created_at, nrip.updated_at).some
        case None => {
          play.api.Logger.warn(("%-20s -->[%s]").format("Balances.updated successfully", "Scaliak returned => None. Thats OK."))
          BalancesResult(nrip.id, nrip.name, nrip.credit, nrip.created_at, nrip.updated_at).some

        }
      }
    }
  }
  
  def findByName(balanceList: Option[List[String]]): ValidationNel[Throwable, BalancesResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Balances", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("BalancesList", balanceList))
    (balanceList map {
      _.map { balanceName =>
        play.api.Logger.debug("models.BalanceName findByName: Balances:" + balanceName)
        (riak.fetch(balanceName) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(balanceName, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              (Validation.fromTryCatch[models.billing.BalancesResult] {
                parse(xs.value).extract[BalancesResult]
              } leftMap { t: Throwable =>
                new ResourceItemNotFound(balanceName, t.getMessage)
              }).toValidationNel.flatMap { j: BalancesResult =>
                Validation.success[Throwable, BalancesResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ? 
              }
            }
            case None => Validation.failure[Throwable, BalancesResults](new ResourceItemNotFound(balanceName, "")).toValidationNel
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((BalancesResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head. 

  }
  
}

