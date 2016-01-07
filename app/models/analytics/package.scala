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

  type WorkbenchesResults = NonEmptyList[Option[WorkbenchesResult]]

  object WorkbenchesResults {
    val emptyNR = List(Option.empty[WorkbenchesResult])
    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: WorkbenchesResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.analytics.WorkbenchesResultsSerialization.{ writer => WorkbenchesResultsWriter }
      toJSON(nres)(WorkbenchesResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: WorkbenchesResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[WorkbenchesResult]) = nels(m)
    def apply(m: WorkbenchesResult): WorkbenchesResults = WorkbenchesResults(m.some)
    def empty: WorkbenchesResults = nel(emptyNR.head, emptyNR.tail)
  }


  type TablesList = List[Tables]

  object TablesList {
    val emptyRR = List(Tables.empty)
    def toJValue(nres: TablesList): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.analytics.TablesListSerialization.{ writer => TablesListWriter }
      toJSON(nres)(TablesListWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[TablesList] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.analytics.TablesListSerialization.{ reader => TablesListReader }
      fromJSON(jValue)(TablesListReader)
    }

    def toJson(nres: TablesList, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[Tables]): TablesList = plansList

    def empty: List[Tables] = emptyRR

  }


  type ConnectorsList = List[Connectors]

  object ConnectorsList {
    val emptyRR = List(Connectors.empty)
    def toJValue(nres: ConnectorsList): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.analytics.ConnectorsListSerialization.{ writer => ConnectorsListWriter }
      toJSON(nres)(ConnectorsListWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[ConnectorsList] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.analytics.ConnectorsListSerialization.{ reader => ConnectorsListReader }
      fromJSON(jValue)(ConnectorsListReader)
    }

    def toJson(nres: ConnectorsList, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[Connectors]): ConnectorsList = plansList

    def empty: List[Connectors] = emptyRR

  }
}
