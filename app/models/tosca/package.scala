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
package models

import scalaz._
import Scalaz._
import scalaz.NonEmptyList
import scalaz.NonEmptyList._
import models.json.tosca.CSARResultsSerialization.{ writer => CSARResultsWriter }

import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import controllers.Constants._

/**
 * @author ram
 *
 */
package object tosca {

  type CSARLinkResults = NonEmptyList[Option[CSARLinkResult]]

  object CSARLinkResults {
    val emptyPC = List(Option.empty[CSARLinkResult])
    def apply(m: CSARLinkResult): CSARLinkResults = nels(m.some)
    def empty: CSARLinkResults = nel(emptyPC.head, emptyPC.tail)
  }

  type CSARResults = NonEmptyList[Option[CSARResult]]

  object CSARResults {
    val emptyPC = List(Option.empty[CSARResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(prres: CSARResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      toJSON(prres)(CSARResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: CSARResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: CSARResult): CSARResults = nels(m.some)
    def empty: CSARResults = nel(emptyPC.head, emptyPC.tail)
  }

  type ComponentsList = List[Component]

  type AssemblysList = List[Assembly]

  type AssemblysLists = NonEmptyList[Option[AssemblyResult]]

  object AssemblysLists {
    val emptyNR = List(Option.empty[AssemblyResult])
    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: AssemblysLists): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.carton.AssemblysListsSerialization.{ writer => AssemblysListsWriter }
      toJSON(nres)(AssemblysListsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: AssemblysLists, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[AssemblyResult]) = nels(m)
    def apply(m: AssemblyResult): AssemblysLists = AssemblysLists(m.some)
    def empty: AssemblysLists = nel(emptyNR.head, emptyNR.tail)
  }

  type AssembliesResults = NonEmptyList[Option[AssembliesResult]]

  object AssembliesResults {
    val emptyNR = List(Option.empty[AssembliesResult])
    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: AssembliesResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.carton.AssembliesResultsSerialization.{ writer => AssembliesResultsWriter }
      toJSON(nres)(AssembliesResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: AssembliesResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[AssembliesResult]) = nels(m)
    def apply(m: AssembliesResult): AssembliesResults = AssembliesResults(m.some)
    def empty: AssembliesResults = nel(emptyNR.head, emptyNR.tail)
  }

  type AssemblyLinks = List[String]

  object AssemblyLinks {
    val emptyRR = List("")

    def toJValue(nres: AssemblyLinks): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.carton.AssemblyLinksSerialization.{ writer => AssemblyLinksWriter }
      toJSON(nres)(AssemblyLinksWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[AssemblyLinks] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.carton.AssemblyLinksSerialization.{ reader => AssemblyLinksReader }
      fromJSON(jValue)(AssemblyLinksReader)
    }

    def toJson(nres: AssemblyLinks, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(assemblysList: List[String]): AssemblyLinks = assemblysList

    def empty: List[String] = emptyRR

  }

  type AssemblyResults = NonEmptyList[Option[AssemblyResult]]

  object AssemblyResults {
    val emptyNR = List(Option.empty[AssemblyResult])
    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: AssemblyResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.carton.AssemblyResultsSerialization.{ writer => AssemblyResultsWriter }
      toJSON(nres)(AssemblyResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: AssemblyResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[AssemblyResult]) = nels(m)
    def apply(m: AssemblyResult): AssemblyResults = AssemblyResults(m.some)
    def empty: AssemblyResults = nel(emptyNR.head, emptyNR.tail)
  }

  type ComponentLinks = List[String]

  object ComponentLinks {
    val emptyRR = List("")
    def toJValue(nres: ComponentLinks): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.carton.ComponentLinksSerialization.{ writer => ComponentLinksWriter }
      toJSON(nres)(ComponentLinksWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[ComponentLinks] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.carton.ComponentLinksSerialization.{ reader => ComponentLinksReader }
      fromJSON(jValue)(ComponentLinksReader)
    }

    def toJson(nres: ComponentLinks, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[String]): ComponentLinks = plansList

    def empty: List[String] = emptyRR

  }

  type ComponentsResults = NonEmptyList[Option[ComponentResult]]

  object ComponentsResults {
    val emptyNR = List(Option.empty[ComponentResult])
    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: ComponentsResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.box.ComponentsResultsSerialization.{ writer => ComponentsResultsWriter }
      toJSON(nres)(ComponentsResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: ComponentsResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[ComponentResult]) = nels(m)
    def apply(m: ComponentResult): ComponentsResults = ComponentsResults(m.some)
    def empty: ComponentsResults = nel(emptyNR.head, emptyNR.tail)
  }

  type SensorsResults = NonEmptyList[Option[SensorsResult]]

  object SensorsResults {
    val emptyNR = List(Option.empty[SensorsResult])
    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: SensorsResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.sensors.SensorsResultsSerialization.{ writer => SensorsResultsWriter }
      toJSON(nres)(SensorsResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: SensorsResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[SensorsResult]) = nels(m)
    def apply(m: SensorsResult): SensorsResults = SensorsResults(m.some)
    def empty: SensorsResults = nel(emptyNR.head, emptyNR.tail)
  }

  type PoliciesList = List[Policy]

  object PoliciesList {
    val emptyRR = List(Policy.empty)
    def toJValue(nres: PoliciesList): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.carton.PoliciesListSerialization.{ writer => PoliciesListWriter }
      toJSON(nres)(PoliciesListWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[PoliciesList] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.carton.PoliciesListSerialization.{ reader => PoliciesListReader }
      fromJSON(jValue)(PoliciesListReader)
    }

    def toJson(nres: PoliciesList, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[Policy]): PoliciesList = plansList

    def empty: List[Policy] = emptyRR

  }

  type MembersList = List[String]

  object MembersList {
    val emptyRR = List("")
    def toJValue(nres: MembersList): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.carton.MembersListSerialization.{ writer => MembersListWriter }
      toJSON(nres)(MembersListWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[MembersList] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.carton.MembersListSerialization.{ reader => MembersListReader }
      fromJSON(jValue)(MembersListReader)
    }

    def toJson(nres: MembersList, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[String]): MembersList = plansList

    def empty: List[String] = emptyRR

  }

  type KeyValueList = List[KeyValueField]

  object KeyValueList {
    val OJA_EMAIL = "email"
    val OJA_API_KEY = "api_key"
    val OJA_ASSEMBLY_ID = "assembly_id"
    val OJA_COMP_ID = "component_id"
    val OJA_SPARK_JOBSERVER = "spark_jobserver"

    val MKT_FLAG_EMAIL = "<email>"
    val MKT_FLAG_APIKEY = "<api_key>"
    val MKT_FLAG_ASSEMBLY_ID = "<assembly_id>"
    val MKT_FLAG_COMP_ID = "<component_id>"
    val MKT_FLAG_HOST = "<host>"

    val emptyRR = List(KeyValueField.empty)

    def toJValue(nres: KeyValueList): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.KeyValueListSerialization.{ writer => KeyValueListWriter }
      toJSON(nres)(KeyValueListWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[KeyValueList] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.KeyValueListSerialization.{ reader => KeyValueListReader }
      fromJSON(jValue)(KeyValueListReader)
    }

    def toJson(nres: KeyValueList, prettyPrint: Boolean = false, flagsMap: Map[String, String] = Map()): String = {
      val nrec = nres.map { x => KeyValueField(x.key, flagsMap.get(x.value).getOrElse(x.value)) }
      if (prettyPrint) {
        prettyRender(toJValue(nrec))
      } else {
        compactRender(toJValue(nrec))
      }
    }

    def apply(plansList: List[KeyValueField]): KeyValueList = plansList

    def empty: List[KeyValueField] = emptyRR

    def toMap(nres: KeyValueList) = (nres.map {x => (x.key, x.value)}).toMap

  }

  type OperationList = List[Operation]

  object OperationList {
    val emptyRR = List(Operation.empty)
    def toJValue(nres: OperationList): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.OperationListSerialization.{ writer => OperationListWriter }
      toJSON(nres)(OperationListWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[OperationList] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.OperationListSerialization.{ reader => OperationListReader }
      fromJSON(jValue)(OperationListReader)
    }

    def toJson(nres: OperationList, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[Operation]): OperationList = plansList

    def empty: List[Operation] = emptyRR

  }

  type MetricList = List[Metric]

  object MetricList {
    val emptyRR = List(Metric.empty)
    def toJValue(nres: MetricList): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.sensors.MetricListSerialization.{ writer => MetricListWriter }
      toJSON(nres)(MetricListWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[MetricList] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.sensors.MetricListSerialization.{ reader => MetricListReader }
      fromJSON(jValue)(MetricListReader)
    }

    def toJson(nres: MetricList, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[Metric]): MetricList = plansList

    def empty: List[Metric] = emptyRR

  }

  type BindLinks = List[String]

  object BindLinks {
    val emptyRR = List("")
    def toJValue(nres: BindLinks): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.box.BindLinksSerialization.{ writer => BindLinksWriter }
      toJSON(nres)(BindLinksWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[BindLinks] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.box.BindLinksSerialization.{ reader => BindLinksReader }
      fromJSON(jValue)(BindLinksReader)
    }

    def toJson(nres: BindLinks, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[String]): BindLinks = plansList

    def empty: List[String] = emptyRR

  }
}
