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
import scalaz._
import Scalaz._
import scalaz.NonEmptyList
import scalaz.NonEmptyList._
import models.json._

import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import controllers.Constants._

/**
 * @author rajthilak
 *
 */
package object models {

  type NodeResults = NonEmptyList[Option[NodeResult]]

  object NodeResults {
    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: NodeResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.NodeResultsSerialization.{ writer => NodeResultsWriter }
      toJSON(nres)(NodeResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: NodeResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[NodeResult]) = nels(m)
    def apply(m: NodeResult): NodeResults = NodeResults(m.some)
    def empty: NodeResults = nels(none)
  }

  type PredefResults = NonEmptyList[Option[PredefResult]]

  object PredefResults {

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(pres: PredefResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.PredefResultsSerialization.{ writer => PredefResultsWriter }
      toJSON(pres)(PredefResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(pres: PredefResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(pres)))
    } else {
      compactRender(toJValue(pres))
    }

    def apply(m: PredefResult): PredefResults = nels(m.some)
    def empty: PredefResults = nels(none)

  }

  type PredefCloudResults = NonEmptyList[Option[PredefCloudResult]]

  object PredefCloudResults {

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(prres: PredefCloudResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.PredefCloudResultsSerialization.{ writer => PredefCloudResultsWriter }
      toJSON(prres)(PredefCloudResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: PredefCloudResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: PredefCloudResult): PredefCloudResults = nels(m.some)
    def empty: PredefCloudResults = nels(none)
  }

  type CloudToolResults = NonEmptyList[Option[CloudTool]]

  object CloudToolResults {

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(cdres: CloudToolResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.CloudToolResultsSerialization.{ writer => CloudToolsWriter }
      toJSON(cdres)(CloudToolsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(cdres: CloudToolResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(cdres)))
    } else {
      compactRender(toJValue(cdres))
    }

    def apply(m: CloudTool): CloudToolResults = nels(m.some)
    def empty: CloudToolResults = nels(none)

  }

  type CloudTemplates = List[CloudTemplate]
  object CloudTemplates {

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(cdres: CloudTemplates): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.CloudTemplatesSerialization.{ writer => CloudTemplatesWriter }
      toJSON(cdres)(CloudTemplatesWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(cdres: CloudTemplates, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(cdres)))
    } else {
      compactRender(toJValue(cdres))
    }

    def apply(ht: CloudTemplate*): CloudTemplates = ht.toList
    def apply(m: CloudTemplate): CloudTemplates = List[CloudTemplate](m)
    def empty: CloudTemplates = List[CloudTemplate]()

  }

  type CloudInstructions = List[CloudInstruction]

  object CloudInstructions {
    
    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[CloudInstructions] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.CloudInstructionsSerialization
      val preser = new CloudInstructionsSerialization()
      fromJSON(jValue)(preser.reader)
    }

    def fromJson(json: String): Result[CloudInstructions] = (Validation.fromTryCatch {
      parse(json)
    } leftMap { t: Throwable =>
      UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
    }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(cdres: CloudInstructions): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.CloudInstructionsSerialization
      val preser = new CloudInstructionsSerialization()
      toJSON(cdres)(preser.writer)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(cdres: CloudInstructions, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(cdres)))
    } else {
      compactRender(toJValue(cdres))
    }

    def apply(ht: CloudInstruction*): CloudInstructions = ht.toList
    def apply(m: CloudInstruction): CloudInstructions = List[CloudInstruction](m)
    def empty: CloudInstructions = List[CloudInstruction]()

  }

  type CloudInstructionGroup = Map[String, CloudInstructions]

  object CloudInstructionGroup {

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[CloudInstructionGroup] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.CloudInstructionGroupSerialization
      val preser = new CloudInstructionGroupSerialization()
      fromJSON(jValue)(preser.reader)
    }

    def fromJson(json: String): Result[CloudInstructionGroup] = (Validation.fromTryCatch {
      parse(json)
    } leftMap { t: Throwable =>
      UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
    }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(cdres: CloudInstructionGroup): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.CloudInstructionGroupSerialization
      val preser = new CloudInstructionGroupSerialization()
      toJSON(cdres)(preser.writer)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(cdres: CloudInstructionGroup, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(cdres)))
    } else {
      compactRender(toJValue(cdres))
    }

    def apply(m: List[Tuple2[String, CloudInstructions]]): CloudInstructionGroup = m.toMap
    //this isn't needed.
    def apply(k: String, v: CloudInstructions): CloudInstructionGroup = Map[String, CloudInstructions](k -> v)
    def empty: CloudInstructionGroup = Map[String, CloudInstructions]()

  }

  implicit def transformNodeResults2Json(nres: NodeResults): Option[String] = NodeResults.toJson(nres, true).some
  implicit def transformPredefResults2Json(pres: PredefResults): Option[String] = PredefResults.toJson(pres, true).some
  implicit def transformPredefCloudResults22Json(prres: PredefCloudResults): Option[String] = PredefCloudResults.toJson(prres, true).some

}