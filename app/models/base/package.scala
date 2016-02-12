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
import models.Constants._

/**
 * @author rajthilak
 *
 */
package object base {

  type RequestResults = NonEmptyList[Option[RequestResult]]

  object RequestResults {
    val emptyRR = List(Option.empty[RequestResult])

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJValue(nres: RequestResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import RequestResultsSerialization.{ writer => RequestResultsWriter }
      toJSON(nres)(RequestResultsWriter)
    }

    //screwy. you pass an instance. may be FunnelResponses needs be to a case class
    def toJson(nres: RequestResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: Option[RequestResult]) = nels(m)
    def apply(m: RequestResult): RequestResults = RequestResults(m.some)
    def empty: RequestResults = nel(emptyRR.head, emptyRR.tail)
  }

  type SshKeyResults = NonEmptyList[Option[SshKeyResult]]

  object SshKeyResults {
    val emptyPC = List(Option.empty[SshKeyResult])

    def toJValue(prres: SshKeyResults): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import SshKeyResultsSerialization.{ writer => SshKeyResultsWriter }
      toJSON(prres)(SshKeyResultsWriter)
    }

    def toJson(nres: SshKeyResults, prettyPrint: Boolean = false): String = if (prettyPrint) {
      prettyRender(toJValue(nres))
    } else {
      compactRender(toJValue(nres))
    }

    def apply(m: SshKeyResult): SshKeyResults = nels(m.some)
    def empty: SshKeyResults = nel(emptyPC.head, emptyPC.tail)
  }

  

  type MarketPlaceSacks = NonEmptyList[Option[MarketPlaceSack]]

  object MarketPlaceSacks {
    val emptyPC = List(Option.empty[MarketPlaceSack])

    def toJValue(prres: MarketPlaceSacks): JValue = {
      import net.liftweb.json.scalaz.JsonScalaz.toJSON
      import MarketPlaceSacksSerialization.{ writer => MarketPlaceSacksWriter }
      toJSON(prres)(MarketPlaceSacksWriter)
    }

    def toJson(nres: MarketPlaceSacks, prettyPrint: Boolean = false): String = {
      //if (prettyPrint) { prettyRender(toJValue(nres))} else {compatRender(toJValue(nres))}
      prettyPrint match {
        case (prettyPrint) => prettyRender(toJValue(nres))
        case _ => compactRender(toJValue(nres))
      }
    }

    def apply(m: MarketPlaceSack): MarketPlaceSacks = nels(m.some)
    def empty: MarketPlaceSacks = nel(emptyPC.head, emptyPC.tail)
  }

}
