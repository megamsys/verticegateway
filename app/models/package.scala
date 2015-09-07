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
package object models {


  type RequestResults = NonEmptyList[Option[RequestResult]]

  object RequestResults {
    val emptyRR = List(Option.empty[RequestResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: RequestResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.RequestResultsSerialization.{ writer => RequestResultsWriter }
      toJSON(nres)(RequestResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: RequestResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[RequestResult]) = nels(m)
    def apply(m: RequestResult): RequestResults = RequestResults(m.some)
    def empty: RequestResults = nel(emptyRR.head, emptyRR.tail)
  }

//  implicit def transformPredefCloudResults22Json(prres: PredefCloudResults): Option[String] = PredefCloudResults.toJson(prres, true).some


  type CloudToolSettingResults = NonEmptyList[Option[CloudToolSettingResult]]

  object CloudToolSettingResults {
    val emptyPC = List(Option.empty[CloudToolSettingResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(prres: CloudToolSettingResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.CloudToolSettingResultsSerialization.{ writer => CloudToolSettingResultsWriter }
      toJSON(prres)(CloudToolSettingResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: CloudToolSettingResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: CloudToolSettingResult): CloudToolSettingResults = nels(m.some)
    def empty: CloudToolSettingResults = nel(emptyPC.head, emptyPC.tail)
  }

  type SshKeyResults = NonEmptyList[Option[SshKeyResult]]

  object SshKeyResults {
    val emptyPC = List(Option.empty[SshKeyResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(prres: SshKeyResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.SshKeyResultsSerialization.{ writer => SshKeyResultsWriter }
      toJSON(prres)(SshKeyResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: SshKeyResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: SshKeyResult): SshKeyResults = nels(m.some)
    def empty: SshKeyResults = nel(emptyPC.head, emptyPC.tail)
  }

  type MarketPlaceResults = NonEmptyList[Option[MarketPlaceResult]]

  object MarketPlaceResults {
    val emptyPC = List(Option.empty[MarketPlaceResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(prres: MarketPlaceResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.MarketPlaceResultsSerialization.{ writer => MarketPlaceResultsWriter }
      toJSON(prres)(MarketPlaceResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: MarketPlaceResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: MarketPlaceResult): MarketPlaceResults = nels(m.some)
    def empty: MarketPlaceResults = nel(emptyPC.head, emptyPC.tail)
  }

  type MarketPlaceAddonsResults = NonEmptyList[Option[MarketPlaceAddonsResult]]

  object MarketPlaceAddonsResults {
    val emptyRR = List(Option.empty[MarketPlaceAddonsResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: MarketPlaceAddonsResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.MarketPlaceAddonsResultsSerialization.{ writer => MarketPlaceAddonsResultsWriter }
      toJSON(nres)(MarketPlaceAddonsResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: MarketPlaceAddonsResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[MarketPlaceAddonsResult]) = nels(m)
    def apply(m: MarketPlaceAddonsResult): MarketPlaceAddonsResults = MarketPlaceAddonsResults(m.some)
    def empty: MarketPlaceAddonsResults = nel(emptyRR.head, emptyRR.tail)
  }

  type MarketPlaceAddonsConfigurationResults = NonEmptyList[Option[MarketPlaceAddonsConfigurationResult]]

  object MarketPlaceAddonsConfigurationResults {
    val emptyRR = List(Option.empty[MarketPlaceAddonsConfigurationResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: MarketPlaceAddonsConfigurationResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.MarketPlaceAddonsConfigurationResultsSerialization.{ writer => MarketPlaceAddonsConfigurationResultsWriter }
      toJSON(nres)(MarketPlaceAddonsConfigurationResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: MarketPlaceAddonsConfigurationResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[MarketPlaceAddonsConfigurationResult]) = nels(m)
    def apply(m: MarketPlaceAddonsConfigurationResult): MarketPlaceAddonsConfigurationResults = MarketPlaceAddonsConfigurationResults(m.some)
    def empty: MarketPlaceAddonsConfigurationResults = nel(emptyRR.head, emptyRR.tail)
  }

  type MarketPlacePlans = List[MarketPlacePlan]

  object MarketPlacePlans {
    val emptyRR = List(MarketPlacePlan.empty)
    def toJValue(nres: MarketPlacePlans): JValue = {

      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import models.json.MarketPlacePlansSerialization.{ writer => MarketPlacePlansWriter }
      toJSON(nres)(MarketPlacePlansWriter)
    }

    def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[MarketPlacePlans] = {
      import net.liftweb.json.scalaz.JsonScalaz.fromJSON
      import models.json.MarketPlacePlansSerialization.{ reader => MarketPlacePlansReader }
      fromJSON(jValue)(MarketPlacePlansReader)
    }

    def toJson(nres: MarketPlacePlans, prettyPrint: Boolean = false): String = if (prettyPrint) {
      pretty(render(toJValue(nres)))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(plansList: List[MarketPlacePlan]): MarketPlacePlans = plansList

    def empty: List[MarketPlacePlan] = emptyRR

  }


}
