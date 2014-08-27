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

  type AssembliesResults = NonEmptyList[Option[AssembliesResult]]

  object AssembliesResults {
    val emptyRR = List(Option.empty[AssembliesResult])

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
    def empty: AssembliesResults = nel(emptyRR.head, emptyRR.tail)
  }
  
   type AssembliesList = List[Assembly]

  object AssembliesList {
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
    
    def apply(plansList: List[Assembly]): AssembliesList = { println(plansList); plansList }

    def empty: List[Assembly] = emptyRR

  }
   
    type Components = List[Component]

  object Components {
    val emptyRR = List(Component.empty)
    def toJValue(nres: Components): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.ComponentsSerialization.{ writer => ComponentsWriter }
      toJSON(nres)(ComponentsWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[Components] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.ComponentsSerialization.{ reader => ComponentsReader }
      fromJSON(jValue)(ComponentsReader)
    }

    def toJson(nres: Components, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }
    
    def apply(plansList: List[Component]): Components = plansList

    def empty: List[Component] = emptyRR

  }
   
 /*  type AssembliesResultList = List[String]

  object AssembliesResultList {
    val emptyRR = List(String.empty)
    def toJValue(nres: AssembliesResultList): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.tosca.AssembliesResultListSerialization.{ writer => AssembliesResultListWriter }
      toJSON(nres)(AssembliesResultListWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[AssembliesResultList] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.tosca.AssembliesResultListSerialization.{ reader => AssembliesResultListReader }
      fromJSON(jValue)(AssembliesResultListReader)
    }

    def toJson(nres: AssembliesResultList, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }
    
    def apply(plansList: List[String]): AssembliesResultList = plansList

    def empty: List[String] = emptyRR

  }*/
  

}