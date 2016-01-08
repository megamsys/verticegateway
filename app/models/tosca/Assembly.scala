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
import scala.collection.mutable.ListBuffer

import cache._
import db._
import models.json.tosca._
import models.json.tosca.carton._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import app.MConfig
import models.base._
import models.tosca._

import org.megam.util.Time
import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import org.megam.common.riak.GunnySack

import org.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author rajthilak
 *
 */
case class Operation(operation_type: String, description: String, properties: models.tosca.KeyValueList, status: String) {
  val json = "{\"operation_type\":\"" + operation_type + "\",\"description\":\"" + description + "\",\"properties\":" + KeyValueList.toJson(properties, true) + ",\"status\":\"" + status + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new OperationSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }

}

object Operation {
  def empty: Operation = new Operation(new String(), new String(), KeyValueList.empty, new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[Operation] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    val preser = new OperationSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[Operation] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}
case class AssemblyResult(id: String, name: String, components: models.tosca.ComponentLinks, tosca_type: String, policies: models.tosca.PoliciesList, inputs: models.tosca.KeyValueList, outputs: models.tosca.KeyValueList, status: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.tosca.carton.AssemblyResultSerialization
    val preser = new models.json.tosca.carton.AssemblyResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }

}

object AssemblyResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[AssemblyResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.tosca.carton.AssemblyResultSerialization
    val preser = new AssemblyResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[AssemblyResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    t.printStackTrace();
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

case class Policy(name: String, ptype: String, members: models.tosca.MembersList) {
  val json = "{\"name\":\"" + name + "\",\"ptype\":\"" + ptype + "\",\"members\":" + MembersList.toJson(members, true) + "}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new PolicySerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }

}

object Policy {
  def empty: Policy = new Policy(new String(), new String(), MembersList.empty)

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[Policy] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    val preser = new PolicySerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[Policy] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

case class Assembly(name: String,
    components: models.tosca.ComponentsList,
    tosca_type: String,
    policies: models.tosca.PoliciesList,
    inputs: models.tosca.KeyValueList,
    outputs: models.tosca.KeyValueList,
    status: String) {
  val json = "{\"name\":\"" + name + "\",\"components\":" + ComponentsList.toJson(components, true) + ",\"tosca_type\":\"" + tosca_type + "\", \"policies\":" + PoliciesList.toJson(policies, true) +
    ",\"inputs\":" + KeyValueList.toJson(inputs, true) + ", \"outputs\":" + KeyValueList.toJson(outputs, true) + ",\"status\":\"" + status + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new AssemblySerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

case class AssemblyUpdateInput(id: String,
    name: String,
    components: models.tosca.ComponentLinks,
    tosca_type: String,
    policies: models.tosca.PoliciesList,
    inputs: models.tosca.KeyValueList,
    outputs: models.tosca.KeyValueList, status: String) {
  val json = "{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"components\":" + ComponentLinks.toJson(components, true) + ",\"tosca_type\":\"" + tosca_type + "\", \"policies\":" + PoliciesList.toJson(policies, true) +
    ",\"inputs\":" + KeyValueList.toJson(inputs, true) + ", \"outputs\":" + KeyValueList.toJson(outputs, true) + ",\"status\":\"" + status + "\"}"
}

case class WrapAssemblyResult(thatGS: Option[AssemblyResult]) {

  implicit val formats = DefaultFormats


  val asm = thatGS.get
  val cattype = asm.tosca_type.split('.')(1)
  val domain = asm.inputs.find(_.key.equalsIgnoreCase(DOMAIN))
  val alma = asm.name + "." + domain.get.value //None is ignored here. dangerous.

}

object Assembly {
  implicit val formats = DefaultFormats
  private val riak = GWRiak("assembly")

  val metadataKey = "Assembly"
  val metadataVal = "Assembly Creation"
  val bindex = "assembly"

  def empty: Assembly = new Assembly(new String(), ComponentsList.empty, new String(), PoliciesList.empty, KeyValueList.empty, KeyValueList.empty, new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[Assembly] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    val preser = new AssemblySerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[Assembly] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

  def findById(assemblyID: Option[List[String]]): ValidationNel[Throwable, AssemblyResults] = {
    (assemblyID map {
      _.map { asm_id =>
        play.api.Logger.debug(("%-20s -->[%s]").format("Assembly ID", asm_id))
        (riak.fetch(asm_id) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(asm_id, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              (AssemblyResult.fromJson(xs.value) leftMap
                { t: NonEmptyList[net.liftweb.json.scalaz.JsonScalaz.Error] =>
                  JSONParsingError(t)
                }).toValidationNel.flatMap { j: AssemblyResult =>
                  play.api.Logger.debug(("%-20s -->[%s]").format("AssemblyResult", j))
                  Validation.success[Throwable, AssemblyResults](nels(j.some)).toValidationNel //screwy kishore, every element in a list ?
                }
            }
            case None => {
              Validation.failure[Throwable, AssemblyResults](new ResourceItemNotFound(asm_id, "")).toValidationNel
            }
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((AssemblyResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head.
  }

  private def updateGunnySack(email: String, input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    val ripNel: ValidationNel[Throwable, AssemblyUpdateInput] = (Validation.fromTryCatchThrowable[AssemblyUpdateInput, Throwable] {
      parse(input).extract[AssemblyUpdateInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      rip <- ripNel
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      asm_collection <- (Assembly.findById(List(rip.id).some) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      val bvalue = Set(aor.get.id)
      val asm = asm_collection.head
      val json = AssemblyResult(rip.id, asm.get.name, asm.get.components, asm.get.tosca_type, rip.policies ::: asm.get.policies, rip.inputs ::: asm.get.inputs, asm.get.outputs, asm.get.status, asm.get.created_at).toJson(false)
      new GunnySack((rip.id), json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some

    }
  }

  def update(email: String, input: String): ValidationNel[Throwable, AssemblyResult] = {
    val ripNel: ValidationNel[Throwable, AssemblyUpdateInput] = (Validation.fromTryCatchThrowable[AssemblyUpdateInput, Throwable] {
      parse(input).extract[AssemblyUpdateInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      rip <- ripNel
      gs <- (updateGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      maybeGS <- (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      maybeGS match {
        case Some(thatGS) =>
          parse(maybeGS.get.value).extract[AssemblyResult]
        case None => {
          play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Assembly.updated successfully", Console.RESET))
          parse(maybeGS.get.value).extract[AssemblyResult]
        }
      }
    }
  }

  def upgrade(email: String, id: String): ValidationNel[Throwable, AssemblyResult] = {
    for {
      asm_collection <- (Assembly.findById(List(id).some) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      val asm = asm_collection.head
      pub(email, WrapAssemblyResult(asm))
    }
  }

  /* Lets clean it up in 1.0 using Messageable  */
  private def pub(email: String, wa: WrapAssemblyResult): AssemblyResult = {
    models.base.Requests.createAndPub(email,
      RequestInput(wa.asm.id, wa.cattype, wa.alma, UPGRADE, OPERTATIONS).json)
    wa.asm
  }
}

object AssemblysList {
  implicit val formats = DefaultFormats

  implicit def AssemblysListsSemigroup: Semigroup[AssemblysLists] = Semigroup.instance((f1, f2) => f1.append(f2))

  val emptyRR = List(Assembly.empty)
  def toJValue(nres: AssemblysList): JValue = {

    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import AssemblysListSerialization.{ writer => AssemblysListWriter }
    toJSON(nres)(AssemblysListWriter)
  }

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[AssemblysList] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import AssemblysListSerialization.{ reader => AssemblysListReader }
    fromJSON(jValue)(AssemblysListReader)
  }

  def toJson(nres: AssemblysList, prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue(nres))
  } else {
    compactRender(toJValue(nres))
  }

  def apply(assemblyList: List[Assembly]): AssemblysList = { assemblyList }

  def empty: List[Assembly] = emptyRR

  private val riak = GWRiak("assembly")
  val metadataKey = "Assembly"
  val metadataVal = "Assembly Creation"
  val bindex = "assembly"

  def createLinks(authBag: Option[controllers.stack.AuthBag], input: AssemblysList): ValidationNel[Throwable, AssemblysLists] = {
    val res = (input map {
      asminp => (create(authBag, asminp))
    }).foldRight((AssemblysLists.empty).successNel[Throwable])(_ +++ _)

    play.api.Logger.debug(("%-20s -->[%s]").format("AssemblysLists", res))
    res.getOrElse(new ResourceItemNotFound(authBag.get.email, "assembly = ah. ouh. for some reason.").failureNel[AssemblysLists])
    res
  }

  /*
   * create new market place item with the 'name' of the item provide as input.
   * A index name assemblies name will point to the "csars" bucket
   */
  def create(authBag: Option[controllers.stack.AuthBag], input: Assembly): ValidationNel[Throwable, AssemblysLists] = {
    for {
      ogsi <- mkGunnySack(authBag, input) leftMap { err: NonEmptyList[Throwable] => err }
      nrip <- AssemblyResult.fromJson(ogsi.get.value) leftMap { t: NonEmptyList[net.liftweb.json.scalaz.JsonScalaz.Error] => nels(JSONParsingError(t)) }
      ogsr <- riak.store(ogsi.get) leftMap { t: NonEmptyList[Throwable] => t }
    } yield {
      ogsr match {
        case Some(thatGS) => {
          nels(AssemblyResult(thatGS.key, nrip.name, nrip.components, nrip.tosca_type, nrip.policies, nrip.inputs, nrip.outputs, nrip.status, Time.now.toString()).some)
        }
        case None => {
          play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Assembly.created successfully", Console.RESET))
          nels(AssemblyResult(ogsi.get.key, nrip.name, nrip.components, nrip.tosca_type, nrip.policies, nrip.inputs, nrip.outputs, nrip.status, Time.now.toString()).some)
        }
      }
    }

  }

  private def mkGunnySack(authBag: Option[controllers.stack.AuthBag], rip: Assembly): ValidationNel[Throwable, Option[GunnySack]] = {
    var outlist = rip.outputs
    for {
      aor <- (Accounts.findByEmail(authBag.get.email) leftMap { t: NonEmptyList[Throwable] => t })
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "asm").get leftMap { ut: NonEmptyList[Throwable] => ut })
      com <- (ComponentsList.createLinks(authBag, rip.components, (uir.get._1 + uir.get._2)) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      val bvalue = Set(aor.get.id)
      var components_links = new ListBuffer[String]()
      if (com.size > 1) {
        for (component <- com) {
          component match {
            case Some(value) => components_links += value.id
            case None => components_links
          }
        }
      }

      val json = AssemblyResult(uir.get._1 + uir.get._2, rip.name, components_links.toList, rip.tosca_type, rip.policies, rip.inputs, outlist, rip.status, Time.now.toString).toJson(false)
      new GunnySack((uir.get._1 + uir.get._2), json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

}
