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

package models.tosca

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import cache._
import db._
import models.json.tosca._
import models.json.tosca.carton._
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._
import app.MConfig
import models.base._
import wash._

import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import io.megam.util.Time
import io.megam.common.riak.GunnySack
import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author rajthilak
 *
 */
case class AssembliesInput(name: String, org_id: String, assemblies: models.tosca.AssemblysList, inputs: KeyValueList) {
  val json = "{\"name\":\"" + name + "\",\"org_id\":\"" + org_id + "\", \"assemblies\":" + AssemblysList.toJson(assemblies, true) + ",\"inputs\":" + KeyValueList.toJson(inputs, true) + "}"
}

case class KeyValueField(key: String, value: String) {
  val json = "{\"key\":\"" + key + "\",\"value\":\"" + value + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new KeyValueFieldSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }

}

object KeyValueField {
  def empty: KeyValueField = new KeyValueField(new String(), new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[KeyValueField] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    val preser = new KeyValueFieldSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[KeyValueField] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

case class AssembliesResult(id: String,
  accounts_id: String,
  name: String,
  assemblies: models.tosca.AssemblyLinks,
  inputs: KeyValueList,
  created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new AssembliesResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }

}

object AssembliesResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[AssembliesResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    val preser = new AssembliesResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[AssembliesResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

case class WrapAssembliesResult(thatGS: Option[GunnySack], idPair: Map[String,String]) {
  
  implicit val formats = DefaultFormats

  val ams = parse(thatGS.get.value).extract[AssembliesResult].some

  def cattype= idPair.map(x => x._2.split('.')(1)).head
}


object Assemblies {

  implicit val formats = DefaultFormats

  private lazy val bucker = "assemblies"

  private lazy val idxedBy = idxTeamId

  private val riak = GWRiak(bucker)

  private def mkGunnySack(authBag: Option[io.megam.auth.stack.AuthBag], input: String): ValidationNel[Throwable, WrapAssembliesResult] = {
    val ripNel: ValidationNel[Throwable, AssembliesInput] = (Validation.fromTryCatchThrowable[AssembliesInput, Throwable] {
      parse(input).extract[AssembliesInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
    for {
      rip <- ripNel
      aor <- (Accounts.findByEmail(authBag.get.email) leftMap { t: NonEmptyList[Throwable] => t })
      ams <- (AssemblysList.createLinks(authBag, rip.assemblies) leftMap { t: NonEmptyList[Throwable] => t })
      uir <- (UID("ams").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val bvalue = Set(aor.get.id, rip.org_id)
      val asml = ams.flatMap { assembly => nels({ assembly.map { a => (a.id, a.tosca_type) } }) }
      val asmlist = asml.toList.filterNot(_.isEmpty)
      val json = new AssembliesResult(uir.get._1 + uir.get._2, aor.get.id, rip.name, asmlist.map(_.get._1), rip.inputs, Time.now.toString).toJson(false)
      new WrapAssembliesResult((new GunnySack((uir.get._1 + uir.get._2), json, RiakConstants.CTYPE_TEXT_UTF8,
      None,Map.empty, Map((idxedBy, bvalue)))).some, asmlist.map(_.get).toMap)
    }
  }

  def create(authBag: Option[io.megam.auth.stack.AuthBag], input: String): ValidationNel[Throwable, AssembliesResult] = {
    (mkGunnySack(authBag, input) leftMap { err: NonEmptyList[Throwable] =>
      new ServiceUnavailableError(input, (err.list.map(m => m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { wa: WrapAssembliesResult =>
      (riak.store(wa.thatGS.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          maybeGS match {
            case Some(thatGS) => wa.ams.get.successNel[Throwable]

            case None => {
              play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Assemblies.created success", Console.RESET))
              pub(authBag.get.email, wa)
            }
          }
        }
    }
  }

  def findById(assembliesID: Option[List[String]]): ValidationNel[Throwable, AssembliesResults] = {
    (assembliesID map {
      _.map { asm_id =>
        play.api.Logger.debug(("%-20s -->[%s]").format("Assemblies Id", asm_id))
        (riak.fetch(asm_id) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(asm_id, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              (AssembliesResult.fromJson(xs.value) leftMap
                { t: NonEmptyList[net.liftweb.json.scalaz.JsonScalaz.Error] =>
                  JSONParsingError(t)
                }).toValidationNel.flatMap { j: AssembliesResult =>
                  play.api.Logger.debug(("%-20s -->[%s]").format("AssembliesResult", j))
                  Validation.success[Throwable, AssembliesResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ?
                }
            }
            case None => {
              Validation.failure[Throwable, AssembliesResults](new ResourceItemNotFound(asm_id, "")).toValidationNel
            }
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((AssembliesResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head.
  }

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email,
   * the csarnames are listed on the index (account.id) in bucket `CSARs`.
   * Using a "csarname" as key, return a list of ValidationNel[List[CSARResult]]
   * Takes an email, and returns a Future[ValidationNel, List[Option[CSARResult]]]
   */
  def findByEmail(email: String): ValidationNel[Throwable, AssembliesResults] = {
    val res = eitherT[IO, NonEmptyList[Throwable], ValidationNel[Throwable, AssembliesResults]] {
      (((for {
        aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t }) //captures failure on the left side, success on right ie the component before the (<-)
      } yield {
        play.api.Logger.debug(("%-20s -->[%s]").format("tosca.Assemblies", "findByEmail" + aor.get.id))
        new GunnySack(idxTeamId, aor.get.id, RiakConstants.CTYPE_TEXT_UTF8,
          None, Map.empty, Map(("",  Set("")))).some
      }) leftMap { t: NonEmptyList[Throwable] => t } flatMap {
        gs: Option[GunnySack] => riak.fetchIndexByValue(gs.get)
      } map { nm: List[String] =>
        (if (!nm.isEmpty) findById(nm.some) else
          new ResourceItemNotFound(email, "Assemblies = nothing found.").failureNel[AssembliesResults])
      }).disjunction).pure[IO]
    }.run.map(_.validation).unsafePerformIO
    res.getOrElse(new ResourceItemNotFound(email, "Assemblies = nothing found.").failureNel[AssembliesResults])
  }

  /* Lets clean it up in 1.0 using Messageable  */
  private def pub(email: String, wa: WrapAssembliesResult): ValidationNel[Throwable, AssembliesResult] = {
    models.base.Requests.createAndPub(email, RequestInput(wa.ams.get.id, wa.cattype,"", CREATE,STATE).json)
    wa.ams.get.successNel[Throwable]
  }
}
