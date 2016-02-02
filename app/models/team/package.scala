/*
** Copyright [2013-2016] [Megam Systems]
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
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import models.json._

import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import controllers.Constants._

/**
 * @author rajthilak
 *
 */
package object team {

type OrganizationsResults = NonEmptyList[Option[OrganizationsResult]]

object OrganizationsResults {
  val emptyPC = List(Option.empty[OrganizationsResult])

  def toJValue(prres: OrganizationsResults): JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.team.OrganizationsResultsSerialization.{ writer => OrganizationsResultsWriter }
    toJSON(prres)(OrganizationsResultsWriter)
  }

  def toJson(nres: OrganizationsResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue(nres))
  } else {
    compactRender(toJValue(nres))
  }

  def apply(m: OrganizationsResult): OrganizationsResults = nels(m.some)
  def empty: OrganizationsResults = nel(emptyPC.head, emptyPC.tail)
}

type DomainsResults = NonEmptyList[Option[DomainsResult]]

object DomainsResults {
  val emptyPC = List(Option.empty[DomainsResult])

  def toJValue(prres: DomainsResults): JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.team.DomainsResultsSerialization.{ writer => DomainsResultsWriter }
    toJSON(prres)(DomainsResultsWriter)
  }

  def toJson(nres: DomainsResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue(nres))
  } else {
    compactRender(toJValue(nres))
  }

  def apply(m: DomainsResult): DomainsResults = nels(m.some)
  def empty: DomainsResults = nel(emptyPC.head, emptyPC.tail)
}

type RelatedOrgsList = List[String]

object RelatedOrgsList {
  val emptyRR = List("")
  def toJValue(nres: RelatedOrgsList): JValue = {

    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.team.RelatedOrgsListSerialization.{ writer => RelatedOrgsListWriter }
    toJSON(nres)(RelatedOrgsListWriter)
  }

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[RelatedOrgsList] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.team.RelatedOrgsListSerialization.{ reader => RelatedOrgsListReader }
    fromJSON(jValue)(RelatedOrgsListReader)
  }

  def toJson(nres: RelatedOrgsList, prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue(nres))
  } else {
    compactRender(toJValue(nres))
  }

  def apply(plansList: List[String]): RelatedOrgsList = plansList

  def empty: List[String] = emptyRR

}
}
