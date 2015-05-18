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
package models

import scalaz._
import Scalaz._
import scalaz.NonEmptyList
import scalaz.NonEmptyList._
import models.json.tosca._


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
      import models.json.tosca.CSARResultsSerialization.{ writer => CSARResultsWriter }
      toJSON(prres)(CSARResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: CSARResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: CSARResult): CSARResults = nels(m.some)
    def empty: CSARResults = nel(emptyPC.head, emptyPC.tail)
  }

  type AssembliesList = List[Assembly]

  type ComponentsList = List[Component]

  type AssembliesLists = NonEmptyList[Option[AssemblyResult]]

  object AssembliesLists {
    val emptyNR = List(Option.empty[AssemblyResult])
    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: AssembliesLists): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.AssembliesListsSerialization.{ writer => AssembliesListsWriter }
      toJSON(nres)(AssembliesListsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: AssembliesLists, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[AssemblyResult]) = nels(m)
    def apply(m: AssemblyResult): AssembliesLists = AssembliesLists(m.some)
    def empty: AssembliesLists = nel(emptyNR.head, emptyNR.tail)
  }

  type AssembliesResults = NonEmptyList[Option[AssembliesResult]]

  object AssembliesResults {
    val emptyNR = List(Option.empty[AssembliesResult])
    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: AssembliesResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.AssembliesResultsSerialization.{ writer => AssembliesResultsWriter }
      toJSON(nres)(AssembliesResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: AssembliesResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
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
      import models.json.tosca.AssemblyLinksSerialization.{ writer => AssemblyLinksWriter }
      toJSON(nres)(AssemblyLinksWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[AssemblyLinks] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.AssemblyLinksSerialization.{ reader => AssemblyLinksReader }
      fromJSON(jValue)(AssemblyLinksReader)
    }

    def toJson(nres: AssemblyLinks, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[String]): AssemblyLinks = plansList

    def empty: List[String] = emptyRR

  }

  type AssemblyResults = NonEmptyList[Option[AssemblyResult]]

  object AssemblyResults {
    val emptyNR = List(Option.empty[AssemblyResult])
    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: AssemblyResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.AssemblyResultsSerialization.{ writer => AssemblyResultsWriter }
      toJSON(nres)(AssemblyResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: AssemblyResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
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
      import models.json.tosca.ComponentLinksSerialization.{ writer => ComponentLinksWriter }
      toJSON(nres)(ComponentLinksWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[ComponentLinks] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.ComponentLinksSerialization.{ reader => ComponentLinksReader }
      fromJSON(jValue)(ComponentLinksReader)
    }

    def toJson(nres: ComponentLinks, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
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
      import models.json.tosca.ComponentsResultsSerialization.{ writer => ComponentsResultsWriter }
      toJSON(nres)(ComponentsResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: ComponentsResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[ComponentResult]) = nels(m)
    def apply(m: ComponentResult): ComponentsResults = ComponentsResults(m.some)
    def empty: ComponentsResults = nel(emptyNR.head, emptyNR.tail)
  }


  type OrganizationsResults = NonEmptyList[Option[OrganizationsResult]]

  object OrganizationsResults {
    val emptyPC = List(Option.empty[OrganizationsResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(prres: OrganizationsResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.OrganizationsResultsSerialization.{ writer => OrganizationsResultsWriter }
      toJSON(prres)(OrganizationsResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: OrganizationsResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: OrganizationsResult): OrganizationsResults = nels(m.some)
    def empty: OrganizationsResults = nel(emptyPC.head, emptyPC.tail)
  }

  type DomainsResults = NonEmptyList[Option[DomainsResult]]

  object DomainsResults {
    val emptyPC = List(Option.empty[DomainsResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(prres: DomainsResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.DomainsResultsSerialization.{ writer => DomainsResultsWriter }
      toJSON(prres)(DomainsResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: DomainsResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: DomainsResult): DomainsResults = nels(m.some)
    def empty: DomainsResults = nel(emptyPC.head, emptyPC.tail)
  }

  
  type PoliciesList = List[Policy]

  object PoliciesList {
    val emptyRR = List(Policy.empty)
    def toJValue(nres: PoliciesList): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.PoliciesListSerialization.{ writer => PoliciesListWriter }
      toJSON(nres)(PoliciesListWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[PoliciesList] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.PoliciesListSerialization.{ reader => PoliciesListReader }
      fromJSON(jValue)(PoliciesListReader)
    }

    def toJson(nres: PoliciesList, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
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
      import models.json.tosca.MembersListSerialization.{ writer => MembersListWriter }
      toJSON(nres)(MembersListWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[MembersList] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.MembersListSerialization.{ reader => MembersListReader }
      fromJSON(jValue)(MembersListReader)
    }

    def toJson(nres: MembersList, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[String]): MembersList = plansList

    def empty: List[String] = emptyRR

  }


  type KeyValueList = List[KeyValueField]

  object KeyValueList {
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

    def toJson(nres: KeyValueList, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[KeyValueField]): KeyValueList = plansList

    def empty: List[KeyValueField] = emptyRR

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
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[Operation]): OperationList = plansList

    def empty: List[Operation] = emptyRR

  }

  type ContiniousIntegrationResults = NonEmptyList[Option[ContiniousIntegrationResult]]

  object ContiniousIntegrationResults {
    val emptyPC = List(Option.empty[ContiniousIntegrationResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(prres: ContiniousIntegrationResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.ContiniousIntegrationResultsSerialization.{ writer => ContiniousIntegrationResultsWriter }
      toJSON(prres)(ContiniousIntegrationResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: ContiniousIntegrationResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: ContiniousIntegrationResult): ContiniousIntegrationResults = nels(m.some)
    def empty: ContiniousIntegrationResults = nel(emptyPC.head, emptyPC.tail)
  }

  type BindLinks = List[String]

  object BindLinks {
    val emptyRR = List("")
    def toJValue(nres: BindLinks): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.BindLinksSerialization.{ writer => BindLinksWriter }
      toJSON(nres)(BindLinksWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[BindLinks] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.BindLinksSerialization.{ reader => BindLinksReader }
      fromJSON(jValue)(BindLinksReader)
    }

    def toJson(nres: BindLinks, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[String]): BindLinks = plansList

    def empty: List[String] = emptyRR

  }


}
