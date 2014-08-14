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


}