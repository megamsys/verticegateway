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
import models.json.tosca.CSARResultsSerialization.{ writer => CSARResultsWriter }


import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import controllers.Constants._



/**
 * @author ranjitha
 *
 */
package object analytics {

  type SparkjobsResults = NonEmptyList[Option[SparkjobsResult]]

  object SparkjobsResults {
    val emptyNR = List(Option.empty[SparkjobsResult])
    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: SparkjobsResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.analytics.SparkjobsResultsSerialization.{ writer => SparkjobsResultsWriter }
      toJSON(nres)(SparkjobsResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: SparkjobsResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[SparkjobsResult]) = nels(m)
    def apply(m: SparkjobsResult): SparkjobsResults = SparkjobsResults(m.some)
    def empty: SparkjobsResults = nel(emptyNR.head, emptyNR.tail)
  }
}
