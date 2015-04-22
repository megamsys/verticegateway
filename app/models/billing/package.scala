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
import models.json.billing._


import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import controllers.Constants._

/**
 * @author rajthilak
 *
 */
package object billing {

  type BalancesResults = NonEmptyList[Option[BalancesResult]]

  object BalancesResults {
    val emptyPR = List(Option.empty[BalancesResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(pres: BalancesResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.billing.BalancesResultsSerialization.{ writer => BalancesResultsWriter }
      toJSON(pres)(BalancesResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(pres: BalancesResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(pres)))
    } else {
      compactRender(toJValue(pres))
    }

    def apply(m: BalancesResult): BalancesResults = nels(m.some)
    def empty: BalancesResults = nel(emptyPR.head, emptyPR.tail)

  }
  
  type BillinghistoriesResults = NonEmptyList[Option[BillinghistoriesResult]]

  object BillinghistoriesResults {
    val emptyPR = List(Option.empty[BillinghistoriesResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(pres: BillinghistoriesResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.billing.BillinghistoriesResultsSerialization.{ writer => BillinghistoriesResultsWriter }
      toJSON(pres)(BillinghistoriesResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(pres: BillinghistoriesResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(pres)))
    } else {
      compactRender(toJValue(pres))
    }

    def apply(m: BillinghistoriesResult): BillinghistoriesResults = nels(m.some)
    def empty: BillinghistoriesResults = nel(emptyPR.head, emptyPR.tail)

  }
  
}