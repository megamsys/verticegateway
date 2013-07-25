/* 
** Copyright [2012-2013] [Megam Systems]
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
import scalaz._
import Scalaz._
import scalaz.NonEmptyList
import scalaz.NonEmptyList._
import models.json.{NodeResultsSerialization, PredefResultsSerialization, PredefCloudResultsSerialization}


import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._

/**
 * @author rajthilak
 *
 */
package object models {

  type NodeResults = NonEmptyList[Option[NodeResult]]

  object NodeResults {
    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: NodeResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.NodeResultsSerialization.{ writer => NodeResultsWriter }
      toJSON(nres)(NodeResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: NodeResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[NodeResult]) = nels(m)
    def apply(m: NodeResult): NodeResults = NodeResults(m.some)
    def empty: NodeResults = nels(none)
  }

  type PredefResults = NonEmptyList[Option[PredefResult]]

  object PredefResults {

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(pres: PredefResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.PredefResultsSerialization.{ writer => PredefResultsWriter }
      toJSON(pres)(PredefResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(pres: PredefResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(pres)))
    } else {
      compactRender(toJValue(pres))
    }

    def apply(m: PredefResult): PredefResults = nels(m.some)
    def empty: PredefResults = nels(none)

  }

  type PredefCloudResults = NonEmptyList[Option[PredefCloudResult]]

  object PredefCloudResults {

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(prres: PredefCloudResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.PredefCloudResultsSerialization.{ writer => PredefCloudResultsWriter }
      toJSON(prres)(PredefCloudResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: PredefCloudResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: PredefCloudResult): PredefCloudResults = nels(m.some)
    def empty: PredefCloudResults = nels(none)

  }

  implicit def transformNodeResults2Json(nres: NodeResults): Option[String] = NodeResults.toJson(nres, true).some
  implicit def transformPredefResults2Json(pres: PredefResults): Option[String] = PredefResults.toJson(pres, true).some
  implicit def transformPredefCloudResults22Json(prres: PredefCloudResults): Option[String] = PredefCloudResults.toJson(prres, true).some

}