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
    val emptyNR = List(Option.empty[NodeResult])
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
    def empty: NodeResults = nel(emptyNR.head, emptyNR.tail)
  }
  
  type NodeProcessedResults = NonEmptyList[Option[NodeProcessedResult]]

  object NodeProcessedResults {
    val emptyNR = List(Option.empty[NodeProcessedResult])
    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: NodeProcessedResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.NodeProcessedResultsSerialization.{ writer => NodeProcessedResultsWriter }
      toJSON(nres)(NodeProcessedResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: NodeProcessedResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[NodeProcessedResult]) = nels(m)
    def apply(m: NodeProcessedResult): NodeProcessedResults = NodeProcessedResults(m.some)
    def empty: NodeProcessedResults = nel(emptyNR.head, emptyNR.tail)
  }
  
  type PredefResults = NonEmptyList[Option[PredefResult]]

  object PredefResults {
    val emptyPR = List(Option.empty[PredefResult])

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
    def empty: PredefResults = nel(emptyPR.head, emptyPR.tail)

  }

  type PredefCloudResults = NonEmptyList[Option[PredefCloudResult]]

  object PredefCloudResults {
    val emptyPC = List(Option.empty[PredefCloudResult])

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
    def empty: PredefCloudResults = nel(emptyPC.head, emptyPC.tail)
  }

  type RequestResults = NonEmptyList[Option[RequestResult]]

  object RequestResults {
    val emptyRR = List(Option.empty[RequestResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: RequestResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.RequestResultsSerialization.{ writer => RequestResultsWriter }
      toJSON(nres)(RequestResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: RequestResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[RequestResult]) = nels(m)
    def apply(m: RequestResult): RequestResults = RequestResults(m.some)
    def empty: RequestResults = nel(emptyRR.head, emptyRR.tail)
  }

  type CloudToolResults = NonEmptyList[Option[CloudTool]]

  object CloudToolResults {
    val emptyCT = List(Option.empty[CloudTool])
	  
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
    def empty: CloudToolResults = nel(emptyCT.head, emptyCT.tail)

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

  type AppDefnsResults = NonEmptyList[Option[AppDefnsResult]]

  object AppDefnsResults {
    val emptyRR = List(Option.empty[AppDefnsResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: AppDefnsResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.AppDefnsResultsSerialization.{ writer => AppDefnsResultsWriter }
      toJSON(nres)(AppDefnsResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: AppDefnsResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[AppDefnsResult]) = nels(m)
    def apply(m: AppDefnsResult): AppDefnsResults = AppDefnsResults(m.some)
    def empty: AppDefnsResults = nel(emptyRR.head, emptyRR.tail)
  }
  
  type AppRequestResults = NonEmptyList[Option[AppRequestResult]]

  object AppRequestResults {
    val emptyRR = List(Option.empty[AppRequestResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: AppRequestResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.AppRequestResultsSerialization.{ writer => AppRequestResultsWriter }
      toJSON(nres)(AppRequestResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: AppRequestResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[AppRequestResult]) = nels(m)
    def apply(m: AppRequestResult): AppRequestResults = AppRequestResults(m.some)
    def empty: AppRequestResults = nel(emptyRR.head, emptyRR.tail)
  }
  
  type BoltDefnsResults = NonEmptyList[Option[BoltDefnsResult]]

  object BoltDefnsResults {
    val emptyRR = List(Option.empty[BoltDefnsResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: BoltDefnsResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.BoltDefnsResultsSerialization.{ writer => BoltDefnsResultsWriter }
      toJSON(nres)(BoltDefnsResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: BoltDefnsResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[BoltDefnsResult]) = nels(m)
    def apply(m: BoltDefnsResult): BoltDefnsResults = BoltDefnsResults(m.some)
    def empty: BoltDefnsResults = nel(emptyRR.head, emptyRR.tail)
  }
  
  type BoltRequestResults = NonEmptyList[Option[BoltRequestResult]]

  object BoltRequestResults {
    val emptyRR = List(Option.empty[BoltRequestResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: BoltRequestResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.BoltRequestResultsSerialization.{ writer => BoltRequestResultsWriter }
      toJSON(nres)(BoltRequestResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: BoltRequestResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[BoltRequestResult]) = nels(m)
    def apply(m: BoltRequestResult): BoltRequestResults = BoltRequestResults(m.some)
    def empty: BoltRequestResults = nel(emptyRR.head, emptyRR.tail)
  }
  
  type CloudToolSettingResults = NonEmptyList[Option[CloudToolSettingResult]]

  object CloudToolSettingResults {
    val emptyPC = List(Option.empty[CloudToolSettingResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(prres: CloudToolSettingResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.CloudToolSettingResultsSerialization.{ writer => CloudToolSettingResultsWriter }
      toJSON(prres)(CloudToolSettingResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: CloudToolSettingResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: CloudToolSettingResult): CloudToolSettingResults = nels(m.some)
    def empty: CloudToolSettingResults = nel(emptyPC.head, emptyPC.tail)
  }
  
  type SshKeyResults = NonEmptyList[Option[SshKeyResult]]

  object SshKeyResults {
    val emptyPC = List(Option.empty[SshKeyResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(prres: SshKeyResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.SshKeyResultsSerialization.{ writer => SshKeyResultsWriter }
      toJSON(prres)(SshKeyResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: SshKeyResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: SshKeyResult): SshKeyResults = nels(m.some)
    def empty: SshKeyResults = nel(emptyPC.head, emptyPC.tail)
  }
  
  type MarketPlaceResults = NonEmptyList[Option[MarketPlaceResult]]

  object MarketPlaceResults {
    val emptyPC = List(Option.empty[MarketPlaceResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(prres: MarketPlaceResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.MarketPlaceResultsSerialization.{ writer => MarketPlaceResultsWriter }
      toJSON(prres)(MarketPlaceResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: MarketPlaceResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: MarketPlaceResult): MarketPlaceResults = nels(m.some)
    def empty: MarketPlaceResults = nel(emptyPC.head, emptyPC.tail)
  }
  
  
  type MarketPlaceAddonsResults = NonEmptyList[Option[MarketPlaceAddonsResult]]

  object MarketPlaceAddonsResults {
    val emptyRR = List(Option.empty[MarketPlaceAddonsResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: MarketPlaceAddonsResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.MarketPlaceAddonsResultsSerialization.{ writer => MarketPlaceAddonsResultsWriter }
      toJSON(nres)(MarketPlaceAddonsResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: MarketPlaceAddonsResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[MarketPlaceAddonsResult]) = nels(m)
    def apply(m: MarketPlaceAddonsResult): MarketPlaceAddonsResults = MarketPlaceAddonsResults(m.some)
    def empty: MarketPlaceAddonsResults = nel(emptyRR.head, emptyRR.tail)
  }
  
  type MarketPlaceAddonsConfigurationResults = NonEmptyList[Option[MarketPlaceAddonsConfigurationResult]]

  object MarketPlaceAddonsConfigurationResults {
    val emptyRR = List(Option.empty[MarketPlaceAddonsConfigurationResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: MarketPlaceAddonsConfigurationResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.MarketPlaceAddonsConfigurationResultsSerialization.{ writer => MarketPlaceAddonsConfigurationResultsWriter }
      toJSON(nres)(MarketPlaceAddonsConfigurationResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: MarketPlaceAddonsConfigurationResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[MarketPlaceAddonsConfigurationResult]) = nels(m)
    def apply(m: MarketPlaceAddonsConfigurationResult): MarketPlaceAddonsConfigurationResults = MarketPlaceAddonsConfigurationResults(m.some)
    def empty: MarketPlaceAddonsConfigurationResults = nel(emptyRR.head, emptyRR.tail)
  }
  
}