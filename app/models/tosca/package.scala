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
  
  type ComponentDesignInputsWires = List[String]

  object ComponentDesignInputsWires {
    val emptyRR = List("")
    def toJValue(nres: ComponentDesignInputsWires): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.ComponentDesignInputsWiresSerialization.{ writer => ComponentDesignInputsWiresWriter }
      toJSON(nres)(ComponentDesignInputsWiresWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[ComponentLinks] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.ComponentDesignInputsWiresSerialization.{ reader => ComponentDesignInputsWiresReader }
      fromJSON(jValue)(ComponentDesignInputsWiresReader)
    }

    def toJson(nres: ComponentLinks, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[String]): ComponentDesignInputsWires = plansList

    def empty: List[String] = emptyRR

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

  type CloudSettingsList = List[CloudSetting]
  
  object CloudSettingsList {

     val emptyRR = List(CloudSetting.empty)
     def toJValue(nres: CloudSettingsList): JValue = {
          import net.liftweb.json.scalaz.JsonScalaz.toJSON
          import models.json.tosca.CloudSettingsListSerialization.{ writer => CloudSettingsListWriter }
          toJSON(nres)(CloudSettingsListWriter)
     }

     def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[CloudSettingsList] = {
         import net.liftweb.json.scalaz.JsonScalaz.fromJSON
         import models.json.tosca.CloudSettingsListSerialization.{ reader => CloudSettingsListReader }
         fromJSON(jValue)(CloudSettingsListReader)
     }

    def toJson(nres: CloudSettingsList, prettyPrint: Boolean = false): String = if (prettyPrint) {
        pretty(render(toJValue(nres)))
      } else {
     compactRender(toJValue(nres))
    }

    def apply(csList: List[CloudSetting]): CloudSettingsList = { println(csList); csList }

    def empty: List[CloudSetting] = emptyRR
  }
  
  type CSWiresList = List[String]
  
  object CSWiresList {
    val emptyRR = List("")
    def toJValue(nres: CSWiresList): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.CSWiresListSerialization.{ writer => CSWiresListWriter }
      toJSON(nres)(CSWiresListWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[CSWiresList] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.CSWiresListSerialization.{ reader => CSWiresListReader }
      fromJSON(jValue)(CSWiresListReader)
    }

    def toJson(nres: CSWiresList, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[String]): CSWiresList = plansList

    def empty: List[String] = emptyRR

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
  
  type OutputsList = List[Output]

  object OutputsList {
    val emptyRR = List(Output.empty)
    def toJValue(nres: OutputsList): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.OutputsListSerialization.{ writer => OutputsListWriter }
      toJSON(nres)(OutputsListWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[OutputsList] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.OutputsListSerialization.{ reader => OutputsListReader }
      fromJSON(jValue)(OutputsListReader)
    }

    def toJson(nres: OutputsList, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }
    
    def apply(plansList: List[Output]): OutputsList = plansList

    def empty: List[Output] = emptyRR

  }
  
  type ComponentOthers = List[ComponentOther]

  object ComponentOthers {
    val emptyRR = List(ComponentOther.empty)
    def toJValue(nres: ComponentOthers): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.ComponentOthersSerialization.{ writer => ComponentOthersWriter }
      toJSON(nres)(ComponentOthersWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[ComponentOthers] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.ComponentOthersSerialization.{ reader => ComponentOthersReader }
      fromJSON(jValue)(ComponentOthersReader)
    }

    def toJson(nres: ComponentOthers, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }
    
    def apply(plansList: List[ComponentOther]): ComponentOthers = plansList

    def empty: List[ComponentOther] = emptyRR

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
  
  
}