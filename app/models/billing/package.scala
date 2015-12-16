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
import controllers.Constants._
import models.json.billing._
import models.billing._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

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
      prettyRender(toJValue(pres))
    } else {
      compactRender(toJValue(pres))
    }

    def apply(m: BalancesResult): BalancesResults = nels(m.some)
    def empty: BalancesResults = nel(emptyPR.head, emptyPR.tail)

  }

  type BilledhistoriesResults = NonEmptyList[Option[BilledhistoriesResult]]

  object BilledhistoriesResults {
    val emptyPR = List(Option.empty[BilledhistoriesResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(pres: BilledhistoriesResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.billing.BilledhistoriesResultsSerialization.{ writer => BilledhistoriesResultsWriter }
      toJSON(pres)(BilledhistoriesResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(pres: BilledhistoriesResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(pres))
    } else {
      compactRender(toJValue(pres))
    }

    def apply(m: BilledhistoriesResult): BilledhistoriesResults = nels(m.some)
    def empty: BilledhistoriesResults = nel(emptyPR.head, emptyPR.tail)

  }

  type DiscountsResults = NonEmptyList[Option[DiscountsResult]]

  object DiscountsResults {
    val emptyPC = List(Option.empty[DiscountsResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(prres: DiscountsResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.billing.DiscountsResultsSerialization.{ writer => DiscountsResultsWriter }
      toJSON(prres)(DiscountsResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: DiscountsResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: DiscountsResult): DiscountsResults = nels(m.some)
    def empty: DiscountsResults = nel(emptyPC.head, emptyPC.tail)
  }
  type InvoicesResults = NonEmptyList[Option[InvoicesResult]]

  object InvoicesResults {
    val emptyPC = List(Option.empty[InvoicesResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(prres: InvoicesResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.billing.InvoicesResultsSerialization.{ writer => InvoicesResultsWriter }
      toJSON(prres)(InvoicesResultsWriter)
    }

    def toJson(nres: InvoicesResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: InvoicesResult): InvoicesResults = nels(m.some)
    def empty: InvoicesResults = nel(emptyPC.head, emptyPC.tail)
  }

}
