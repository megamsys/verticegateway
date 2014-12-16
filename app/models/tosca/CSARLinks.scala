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
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
//import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps
import scalaz.NonEmptyList._
import org.megam.util.Time
import controllers.stack._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import controllers.Constants._
import models._
import models.riak._
import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import scala.collection.JavaConversions._
import models.cache._
import org.yaml.snakeyaml.Yaml

/**
 * @author rajthilak
 *
 */

case class CSARLinkInput(kachha: String) {
  val TOSCA_DESCRIPTION = "description"

  lazy val kacchaMango: Validation[Throwable, Map[String, String]] = (Validation.fromTryCatch[Map[String, String]] {
    play.api.Logger.debug(("%-20s -->[%s]").format("tosca.CSARLinks", "kacchaMango:Entry"))
    mapAsScalaMap[String, String](new Yaml().load(kachha).asInstanceOf[java.util.Map[String, String]]).toMap
  } leftMap { t: Throwable => t
  })

  lazy val desc: ValidationNel[Throwable, Option[String]] = (kacchaMango.leftMap { err: Throwable =>
    err
  }).toValidationNel.flatMap { chunk: Map[String, String] => Validation.success[Throwable, Option[String]](chunk.get(TOSCA_DESCRIPTION)).toValidationNel }

}

case class CSARLinkResult(id: String, desc: String)

object CSARLinks {

  implicit val formats = DefaultFormats
  private val riak = GWRiak("csarlinks")
  implicit def CSARsSemigroup: Semigroup[CSARLinkResults] = Semigroup.instance((f1, f2) => f1.append(f2))

  val metadataKey = "csarlinkkey"
  val metadataVal = "csarlinkkeys Creation"
  val bindex = "csarlink"

  /**
   * A private method which chains computation to make GunnySack when provided with an input yaml, email.
   * If there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(email: String, input: String): ValidationNel[Throwable, (String, Option[GunnySack])] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("tosca.CSARLinks", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("yaml", input))

    val csarInput: ValidationNel[Throwable, Option[String]] = CSARLinkInput(input).desc

    for {
      pdc <- csarInput
      //TO-DO: Does the leftMap make sense ? To check during function testing, by removing it.
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "csi").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for None on aor, and uir (confirm it during function testing).
      val bvalue = Set(aor.get.id)
      (pdc.get, (new GunnySack(uir.get._1 + uir.get._2, input, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue)))).some)
    }
  }

  /*
   * create new Node with the 'name' of the node provide as input.
   * A index name accountID will point to the "accounts" bucket
   */
  def create(email: String, input: String): ValidationNel[Throwable, CSARLinkResult] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("tosca.CSARLinks", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("yaml", input))

    (mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { gs: (String, Option[GunnySack]) =>
      (riak.store(gs._2.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => CSARLinkResult(thatGS.key, thatGS.value).successNel[Throwable]
            case None => {
              play.api.Logger.debug(("%-20s -->[%s]").format("desc", gs._1 + "," + gs._2.get))
              play.api.Logger.warn(("%-20s -->[%s]").format("CSARLink.created success", "Scaliak returned => None. Thats OK."))
              CSARLinkResult(gs._2.get.key, gs._1).successNel[Throwable]
            }
          }

        }
    }
  }

  def findByName(csarLinksNameList: Option[List[String]]): ValidationNel[Throwable, CSARLinkResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("tosca.CSARLinks", "findByName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("CSARLinksList", csarLinksNameList))

    (csarLinksNameList map {
      _.map { csarLinkName =>
        play.api.Logger.debug("models.tosca.CSARLinks findByName: CSARLinks:" + csarLinkName)
        (riak.fetch(csarLinkName) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(csarLinkName, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              (Validation.fromTryCatch[models.tosca.CSARLinkResult] {      
                CSARLinkResult(csarLinkName, xs.value)
              } leftMap { t: Throwable =>
                new ResourceItemNotFound(csarLinkName, t.getMessage)
              }).toValidationNel.flatMap { j: CSARLinkResult =>
                Validation.success[Throwable, CSARLinkResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ? 
              }
            }
            case None => Validation.failure[Throwable, CSARLinkResults](new ResourceItemNotFound(csarLinkName, "")).toValidationNel
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((CSARLinkResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head. 

  }

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email, 
   * the csarlinknames are listed on the index (account.id) in bucket `CSARLinks`.
   * Using a "csarlinkname" as key, return a list of ValidationNel[List[CSARLinkResult]] 
   * Takes an email, and returns a Future[ValidationNel, List[Option[CSARLinkResult]]]
   */
  def findByEmail(email: String): ValidationNel[Throwable, CSARLinkResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("tosca.CSARLinks", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Throwable, CSARLinkResults]] {
      (((for {
        aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      } yield {
        val bindex = ""
        val bvalue = Set("")
        new GunnySack("csarlink", aor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
          None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
      }) leftMap { t: NonEmptyList[Throwable] => t } flatMap {
        gs: Option[GunnySack] => riak.fetchIndexByValue(gs.get)
      } map { nm: List[String] =>
        (if (!nm.isEmpty) findByName(nm.some) else
          new ResourceItemNotFound(email, "CSARLinks = nothing found.").failureNel[CSARLinkResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    res.getOrElse(new ResourceItemNotFound(email, "CSARLinks = nothing found.").failureNel[CSARLinkResults])
  }

}