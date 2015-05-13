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
//import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps
import org.megam.util.Time
import scala.collection.mutable.ListBuffer
import controllers.stack._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import models.tosca._
import models._
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


//The operation class is some user operations of APP (like - build, start, stop...)
// The CI operation is also build operation of APP
// The operation type is CI then the operation_requirements field must have followwing fields
//    1.scm
//    2.enable
//    3.token
//    4.accounts_id
//    5.owner
// These required fields for CI operation
//
// The requirements field must have "HOST" field
//
//




case class Operation(operation_type: String, description: String, operation_requirements: models.tosca.KeyValueList) {
  val json = "{\"operation_type\":\"" + operation_type + "\",\"description\":\"" + description + "\",\"operation_requirements\":\"" + KeyValueList.toJson(operation_requirements, true) + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.tosca.OperationSerialization
    val preser = new models.json.tosca.OperationSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }

}

object Operation {
  def empty: Operation = new Operation(new String(), new String(), KeyValueList.empty)
  
   def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[Operation] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.tosca.OperationSerialization
    val preser = new models.json.tosca.OperationSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[Operation] = (Validation.fromTryCatch[net.liftweb.json.JValue] {
    play.api.Logger.debug(("%-20s -->[%s]").format("---json------------------->", json))
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }
  
}

case class AssemblyResult(id: String, name: String, components: models.tosca.ComponentLinks, tosca_type: String, requirements: models.tosca.KeyValueList, policies: models.tosca.PoliciesList, inputs: models.tosca.KeyValueList, operations: OperationList, outputs: models.tosca.KeyValueList, status: String, created_at: String) {
  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.tosca.AssemblyResultSerialization
    val preser = new AssemblyResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object AssemblyResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[AssemblyResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.tosca.AssemblyResultSerialization
    val preser = new AssemblyResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[AssemblyResult] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

case class Policy(name: String, ptype: String, members: models.tosca.MembersList) {
  val json = "{\"name\":\"" + name + "\",\"ptype\":\"" + ptype + "\",\"members\":" + MembersList.toJson(members, true) + "}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new models.json.tosca.PolicySerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }

}

object Policy {
  def empty: Policy = new Policy(new String(), new String(), MembersList.empty)

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[Policy] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    val preser = new models.json.tosca.PolicySerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[Policy] = (Validation.fromTryCatch[net.liftweb.json.JValue] {
    play.api.Logger.debug(("%-20s -->[%s]").format("---json------------------->", json))
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

case class Assembly(name: String, components: models.tosca.ComponentsList, tosca_type: String, requirements: models.tosca.KeyValueList, policies: models.tosca.PoliciesList, inputs: models.tosca.KeyValueList, operations: OperationList, outputs: models.tosca.KeyValueList, status: String) {
  val json = "{\"name\":\"" + name + "\",\"components\":" + ComponentsList.toJson(components, true) + ",\"tosca_type\":\"" + tosca_type + "\",\"requirements\":" + KeyValueList.toJson(requirements, true) + ",\"policies\":" + PoliciesList.toJson(policies, true) +
    ",\"inputs\":" + KeyValueList.toJson(inputs, true) + ",\"operations\":" + OperationList.toJson(operations, true) + ",\"outputs\":" + KeyValueList.toJson(outputs, true) + ",\"status\":\"" + status + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new models.json.tosca.AssemblySerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

case class AssemblyUpdateInput(id: String, name: String, components: models.tosca.ComponentLinks, tosca_type: String, requirements: models.tosca.KeyValueList, policies: models.tosca.PoliciesList, inputs: models.tosca.KeyValueList, operations: OperationList, outputs: models.tosca.KeyValueList, status: String, created_at: String) {
  val json = "{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"components\":" + ComponentLinks.toJson(components, true) + ",\"tosca_type\":\"" + tosca_type + "\",\"requirements\":" + KeyValueList.toJson(requirements, true) + ",\"policies\":" + PoliciesList.toJson(policies, true) +
    ",\"inputs\":" + KeyValueList.toJson(inputs, true) + ",\"operations\":\"" + OperationList.toJson(operations, true) + "\",\"outputs\":" + KeyValueList.toJson(outputs, true) + ",\"status\":\"" + status + "\",\"created_at\":\"" + created_at + "\"}"
}

object Assembly {
  implicit val formats = DefaultFormats
  private val riak = GWRiak("assembly")

  val metadataKey = "ASSEMBLY"
  val metadataVal = "Assembly Creation"
  val bindex = "assembly"

  def empty: Assembly = new Assembly(new String(), ComponentsList.empty, new String(), KeyValueList.empty, PoliciesList.empty, KeyValueList.empty, OperationList.empty, KeyValueList.empty, new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[Assembly] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    val preser = new models.json.tosca.AssemblySerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[Assembly] = (Validation.fromTryCatch {
    play.api.Logger.debug(("%-20s -->[%s]").format("---json------------------->", json))
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

  def findByNodeName(assemblyID: Option[List[String]]): ValidationNel[Throwable, AssemblyResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Assembly", "findByNodeName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("nodeNameList", assemblyID))
    (assemblyID map {
      _.map { asm_id =>
        play.api.Logger.debug(("%-20s -->[%s]").format("Assembly ID", asm_id))
        (riak.fetch(asm_id) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(asm_id, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[GunnySack] =>
          xso match {
            case Some(xs) => {
              //JsonScalaz.Error doesn't descend from java.lang.Error or Throwable. Screwy.
              (AssemblyResult.fromJson(xs.value) leftMap
                { t: NonEmptyList[net.liftweb.json.scalaz.JsonScalaz.Error] =>
                  JSONParsingError(t)
                }).toValidationNel.flatMap { j: AssemblyResult =>
                  play.api.Logger.debug(("%-20s -->[%s]").format("assemblies result", j))
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
    play.api.Logger.debug(("%-20s -->[%s]").format("tosca.Assembly Update", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val ripNel: ValidationNel[Throwable, AssemblyUpdateInput] = (Validation.fromTryCatch {
      parse(input).extract[AssemblyUpdateInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      rip <- ripNel
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      asm_collection <- (Assembly.findByNodeName(List(rip.id).some) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      val bvalue = Set(aor.get.id)
      val asm = asm_collection.head
      val json = AssemblyResult(rip.id, asm.get.name, asm.get.components, asm.get.tosca_type, asm.get.requirements, rip.policies ::: asm.get.policies, rip.inputs ::: asm.get.inputs, rip.operations ::: asm.get.operations, asm.get.outputs, asm.get.status, asm.get.created_at).toJson(false)
      new GunnySack((rip.id), json, RiakConstants.CTYPE_TEXT_UTF8, None,
       Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some        
    
    }
  }
 
  def update(email: String, input: String): ValidationNel[Throwable, Option[Tuple2[Map[String, String], String]]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Assembly", "update:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    val ripNel: ValidationNel[Throwable, AssemblyUpdateInput] = (Validation.fromTryCatch {
      parse(input).extract[AssemblyUpdateInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      rip <- ripNel
      gs <- (updateGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      maybeGS <- (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      val nrip = parse(gs.get.value).extract[AssemblyResult]
      maybeGS match {
        case Some(thatGS) =>
          Tuple2(Map[String, String](("Id" -> nrip.id), ("Action" -> "bind policy"), ("Args" -> "Nah")), nrip.name).some
        case None => {
          play.api.Logger.warn(("%-20s -->[%s]").format("Assembly.updated successfully", "Scaliak returned => None. Thats OK."))
          Tuple2(Map[String, String](("Id" -> nrip.id), ("Action" -> "bind policy"), ("Args" -> "Nah")), nrip.name).some
        }
      }
    }
  }

}

object AssembliesList {
  implicit val formats = DefaultFormats

  implicit def AssembliesListsSemigroup: Semigroup[AssembliesLists] = Semigroup.instance((f1, f2) => f1.append(f2))

  val emptyRR = List(Assembly.empty)
  def toJValue(nres: AssembliesList): JValue = {

    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.tosca.AssembliesListSerialization.{ writer => AssembliesListWriter }
    toJSON(nres)(AssembliesListWriter)
  }

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[AssembliesList] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.tosca.AssembliesListSerialization.{ reader => AssembliesListReader }
    fromJSON(jValue)(AssembliesListReader)
  }

  def toJson(nres: AssembliesList, prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue(nres)))
  } else {
    compactRender(toJValue(nres))
  }

  def apply(assemblyList: List[Assembly]): AssembliesList = { println(assemblyList); assemblyList }

  def empty: List[Assembly] = emptyRR

  private val riak = GWRiak("assembly")

  val metadataKey = "ASSEMBLY"
  val metadataVal = "Assembly Creation"
  val bindex = "assembly"

  def createLinks(email: String, input: AssembliesList): ValidationNel[Throwable, AssembliesLists] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("tosca.AssembliesList", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("yaml", input))

    val res = (input map {
      asminp =>
        play.api.Logger.debug(("%-20s -->[%s]").format("assembly", asminp))
        (create(email, asminp))
    }).foldRight((AssembliesLists.empty).successNel[Throwable])(_ +++ _)

    play.api.Logger.debug(("%-20s -->[%s]").format("models.tosca.Assembly", res))
    res.getOrElse(new ResourceItemNotFound(email, "nodes = ah. ouh. ror some reason.").failureNel[AssembliesLists])
    res
  }

  /*
   * create new market place item with the 'name' of the item provide as input.
   * A index name assemblies name will point to the "csars" bucket
   */
  def create(email: String, input: Assembly): ValidationNel[Throwable, AssembliesLists] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("tosca.AssembliesList", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("yaml", input))

    for {
      ogsi <- mkGunnySack(email, input) leftMap { err: NonEmptyList[Throwable] => err }
      nrip <- AssemblyResult.fromJson(ogsi.get.value) leftMap { t: NonEmptyList[net.liftweb.json.scalaz.JsonScalaz.Error] => println("osgi\n" + ogsi.get.value); play.api.Logger.debug(JSONParsingError(t).toString); nels(JSONParsingError(t)) }
      ogsr <- riak.store(ogsi.get) leftMap { t: NonEmptyList[Throwable] => play.api.Logger.debug("--------> ooo"); t }
    } yield {
      play.api.Logger.debug(("%-20s -->[%s],riak returned: %s").format("Assembly.created successfully", email, ogsr))
      ogsr match {
        case Some(thatGS) => {
          nels(AssemblyResult(thatGS.key, nrip.name, nrip.components, nrip.tosca_type, nrip.requirements, nrip.policies, nrip.inputs, nrip.operations, nrip.outputs, nrip.status, Time.now.toString()).some)
        }
        case None => {
          play.api.Logger.warn(("%-20s -->[%s]").format("Node.created successfully", "Scaliak returned => None. Thats OK."))
          nels(AssemblyResult(ogsi.get.key, nrip.name, nrip.components, nrip.tosca_type, nrip.requirements, nrip.policies, nrip.inputs, nrip.operations, nrip.outputs, nrip.status, Time.now.toString()).some)
        }
      }
    }

  }

  private def mkGunnySack(email: String, rip: Assembly): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("tosca.Assembly", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("email", email))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", rip))
    var outlist = rip.outputs
    for {
      aor <- (Accounts.findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "asm").get leftMap { ut: NonEmptyList[Throwable] => ut })
      com <- (ComponentsList.createLinks(email, rip.components, (uir.get._1 + uir.get._2)) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      val bvalue = Set(aor.get.id)
      var components_links = new ListBuffer[String]()
      if (com.length > 1) {
        for (component <- com) {
          component match {
            case Some(value) => components_links += value.id
            case None => components_links += ""
          }
        }
        if (rip.components(0).tosca_type == "tosca.web.docker") {
          for {
            predef <- (models.PredefClouds.create(email, new PredefCloudInput(rip.name, new PredefCloudSpec("docker", uir.get._1 + uir.get._2, "", "", ""), new PredefCloudAccess("", "", "fedora", "", "", "", "")).json) leftMap { t: NonEmptyList[Throwable] => t })
          } yield {
            outlist :::= List(KeyValueField("container", predef.get.name))
          }
        }
      }
      val json = AssemblyResult(uir.get._1 + uir.get._2, rip.name, components_links.toList, rip.tosca_type, rip.requirements, rip.policies, rip.inputs, rip.operations, outlist, rip.status, Time.now.toString).toJson(false)
      new GunnySack((uir.get._1 + uir.get._2), json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

}
 