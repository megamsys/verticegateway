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

import controllers.funnel.FunnelErrors._
import controllers.stack._
import controllers.Constants._
import com.stackmob.scaliak._
import com.basho.riak.client.query.indexes.{ RiakIndexes, IntIndex, BinIndex }
import com.basho.riak.client.http.util.{ Constants => RiakConstants }
import java.nio.charset.Charset
import models._
import models.cache._
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import scalaz._
import scalaz.effect.IO
import scalaz.syntax.SemigroupOps
import scalaz.EitherT._
import scalaz.NonEmptyList._
import scalaz.Validation._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._

/**
 * @author ram
 *
 */

case class CloudTemplate(cctype: String, cloud_instruction_group: CloudInstructionGroup) {

  val json = "{\"cctype\": \"" + cctype + "\",\"cloud_instruction_group\":" +
    "{" + cloud_instruction_group.map(x => "\"" + x._1 + "\":" + x._2.map(_.json).mkString("[", ",", "]")).mkString(",") + "}}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.CloudTemplateSerialization
    val nrsser = new CloudTemplateSerialization()
    toJSON(this)(nrsser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }

}

object CloudTemplate {

  def apply = new CloudTemplate(new String(), CloudInstructionGroup.empty)
  def empty: CloudTemplate = CloudTemplate.apply

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[CloudTemplate] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.CloudTemplateSerialization
    val nrsser = new CloudTemplateSerialization()
    fromJSON(jValue)(nrsser.reader)
  }

  def fromJson(json: String): Result[CloudTemplate] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

case class CloudInstruction(action: String, command: String, name: String) {
  val json = "{\"action\": \"" + action + "\",\"command\":\"" + command + "\",\"name\":\"" + name + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.CloudInstructionSerialization
    val nrsser = new CloudInstructionSerialization()
    toJSON(this)(nrsser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object CloudInstruction {

  def apply = new CloudInstruction(new String(), new String(), new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[CloudInstruction] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.CloudInstructionSerialization
    val nrsser = new CloudInstructionSerialization()
    fromJSON(jValue)(nrsser.reader)
  }

  def fromJson(json: String): Result[CloudInstruction] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

case class CloudTool(id: String, name: String, cli: String, cloudtemplates: CloudTemplates) {

  val json = "{\"id\": \"" + id + "\",\"name\":\"" + name + "\",\"cli\":\"" + cli +
    "\",\"cloudtemplates\": " + cloudtemplates.map(_.json).mkString("[", ",", "]") + "}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.CloudToolSerialization
    val preser = new CloudToolSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object CloudTool {

  def apply(name: String): CloudTool = new CloudTool("not found", name, new String(), CloudTemplates.empty)

  def apply = new CloudTool(new String(), new String(), new String(), CloudTemplates.empty)

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[CloudTool] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.CloudToolSerialization
    val preser = new CloudToolSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[CloudTool] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

  val ec2 = CloudTemplate("ec2", CloudInstructionGroup(List("server" -> CloudInstructions(
    CloudInstruction("create", "server create -c", "-N"),
    CloudInstruction("delete", "server delete `knife search node name:<node_name> -a megam.instanceid` -c -P -y", "-N"),
    CloudInstruction("list", "server list", "")), "instance" -> CloudInstructions(
    CloudInstruction("data", "instance set", "-N")))))

  val hp = CloudTemplate("hp", CloudInstructionGroup(List("server" -> CloudInstructions(
    CloudInstruction("create", "server create -c", "-N"),
    CloudInstruction("delete", "server delete `knife search node name:<node_name> -a megam.instanceid` -c -P -y", "-N"),
    CloudInstruction("list", "server list", "")), "instance" -> CloudInstructions(
    CloudInstruction("data", "instance set", "-N")))))

  val profitbricks = CloudTemplate("profitbricks", CloudInstructionGroup(List("server" -> CloudInstructions(
    CloudInstruction("create", "server create ", "--name"),
    CloudInstruction("delete", "server delete `knife search node name:<node_name> -a megam.instanceid` -c -P -y", "-N"),
    CloudInstruction("list", "server list", "")), "instance" -> CloudInstructions(
    CloudInstruction("data", "instance set", "-N")))))

  val gce = CloudTemplate("google", CloudInstructionGroup(List("server" -> CloudInstructions(
    CloudInstruction("create", "server create <node_name> -f -c", "-N"),
    CloudInstruction("delete", "server delete `knife search node name:<node_name> -a megam.instanceid` -c -P -y", "-N"),
    CloudInstruction("list", "server list", "")), "instance" -> CloudInstructions(
    CloudInstruction("data", "instance set", "-N")))))

  val rackspace = CloudTemplate("rackspace", CloudInstructionGroup.empty)
  val openstack = CloudTemplate("openstack", CloudInstructionGroup.empty)
  val myiaas = CloudTemplate("myiaas", CloudInstructionGroup.empty)

  val cloudtemplates = CloudTemplates(ec2, rackspace, openstack, hp, gce, profitbricks,myiaas)

  val toMap = Map[String, CloudTool]("chef" -> CloudTool("", "chef", "knife", cloudtemplates))

  val toStream = toMap.keySet.toStream
}

object CloudTools {

  implicit val formats = DefaultFormats
  implicit def CloudToolResultsSemigroup: Semigroup[CloudToolResults] = Semigroup.instance((f1, f2) => f1.append(f2))

  private def riak: GSRiak = GSRiak(MConfig.riakurl, "cloudtools")

  val metadataKey = "CloudTool"
  val metadataVal = "CloudTools Creation"
  val bindex = BinIndex.named("cloudToolName")
  /**
   * to
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * If the unique id is got successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(input: CloudTool): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug("models.CloudTools mkGunnySack: entry:\n" + input.json)
    val cloudDeployerInput: ValidationNel[Throwable, CloudTool] = (Validation.fromTryCatch {
      parse(input.json).extract[CloudTool]
    } leftMap { t: Throwable => new MalformedBodyError(input.json, t.getMessage) }).toValidationNel //capture failure
    for {
      pip <- cloudDeployerInput
      //TO-DO: Does the leftMap make sense ? To check during function testing, by removing it.
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "cto").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for uir to filter the None case. confirm it during function testing.
      play.api.Logger.debug("models.CloudTools mkGunnySack: yield:\n" + (uir.get._1 + uir.get._2))
      val bvalue = Set(pip.name)
      val pipJson = new CloudTool((uir.get._1 + uir.get._2), pip.name, pip.cli, pip.cloudtemplates).json
      new GunnySack(pip.name, pipJson, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /**
   * The create calls and puts stuff in Riak if the CloudTools don't exists for the following
   * akka, java, nodejs, play, postgresql, rails, riak, rabbitmq, redis
   * Every key/value stored in riak has the the name "eg: rails, play" as the key, and an index named
   * cloudDeployerName = "chef" as well.
   */
  def create: ValidationNel[Throwable, CloudToolResults] = {
    play.api.Logger.debug("models.CloudTools create: entry")
    (CloudTool.toMap.some map {
      _.map { p =>
        (mkGunnySack(p._2) leftMap { t: NonEmptyList[Throwable] => t
        }).flatMap { gs: Option[GunnySack] =>
          (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
            flatMap { maybeGS: Option[GunnySack] =>
              maybeGS match {
                case Some(thatGS) => CloudToolResults(parse(thatGS.value).extract[CloudTool]).successNel[Throwable]
                case None => {
                  play.api.Logger.warn(("%-20s -->[%s]").format("CloudTools.created success", "Scaliak returned => None. Thats OK."))
                  CloudToolResults(CloudTool(new String(), p._2.name, p._2.cli,
                    p._2.cloudtemplates)).successNel[Throwable]
                }
              }
            }
        }
      }
    } map {
      _.fold((CloudToolResults.empty).successNel[Throwable])(_ +++ _) //fold or foldRight ? 
    }).head //return the folded element in the head.  

  }

  /*
   * Uses the StateMonad to store the first time fetch of a cloudDeployer in a cache where the stale gets invalidated every 5 mins.
   * The downside of this approach is any new update will have to wait 5 mins
   * Every time a State is run, a new InMemoryCache is used as the initial state. This is ok, as we use InMemoryCache as a fascade 
   * to the actualy play.api.cache.Cache object.
   */
  def findByName(cloudToolsList: Option[Stream[String]]): ValidationNel[Throwable, CloudToolResults] = {
    play.api.Logger.debug("models.CloudTools findByName: entry:")
    play.api.Logger.debug(("%-20s -->[%s]").format("cloudToolsList", cloudToolsList))
    (cloudToolsList map {
      _.map { name =>
        InMemory[ValidationNel[Throwable, CloudToolResults]]({
          cname: String =>
            {
              play.api.Logger.debug("models.CloudTools findByName: InMemory:" + cname)
              (riak.fetch(name) leftMap { t: NonEmptyList[Throwable] =>
                new ServiceUnavailableError(cname, (t.list.map(m => m.getMessage)).mkString("\n"))
              }).toValidationNel.flatMap { xso: Option[GunnySack] =>
                xso match {
                  case Some(xs) => {
                    (Validation.fromTryCatch {
                      parse(xs.value).extract[CloudTool]
                    } leftMap { t: Throwable =>
                      new ResourceItemNotFound(cname, t.getMessage)
                    }).toValidationNel.flatMap { j: CloudTool =>
                      Validation.success[Throwable, CloudToolResults](nels(j.some)).toValidationNel
                    }
                  }
                  case None => Validation.failure[Throwable, CloudToolResults](new ResourceItemNotFound(cname, "Please rerun cloudDeployer initiation again.")).toValidationNel
                }
              }
            }
        }).get(name).eval(InMemoryCache[ValidationNel[Throwable, CloudToolResults]]())
      }
    } map {
      _.fold((CloudToolResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head
  }

  def listAll: ValidationNel[Throwable, CloudToolResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.CloudTools", "listAll:Entry"))
    findByName(CloudTool.toStream.some) //return the folded element in the head.  
  }

  implicit val sedimentCloudToolResults = new Sedimenter[ValidationNel[Throwable, CloudToolResults]] {
    override def sediment(maybeASediment: ValidationNel[Throwable, CloudToolResults]): Boolean = maybeASediment.isSuccess
  }
}