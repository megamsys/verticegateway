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

  type AssembliesResults = NonEmptyList[Option[AssemblyResult]]

  object AssembliesResults {
    val emptyNR = List(Option.empty[AssemblyResult])
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

    def apply(m: Option[AssemblyResult]) = nels(m)
    def apply(m: AssemblyResult): AssembliesResults = AssembliesResults(m.some)
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

}