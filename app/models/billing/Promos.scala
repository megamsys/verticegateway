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

package models.billing

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import cache._
import db._
import models.json.billing._
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._
import app.MConfig

import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import io.megam.common.riak.GunnySack
import io.megam.util.Time
import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author morpheyesh
 *
 */
case class PromosResult(id: String, code: String, amount: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.billing.PromosResultSerialization
    val preser = new PromosResultSerialization()
    toJSON(this)(preser.writer) //where does this JSON from?
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object PromosResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[PromosResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.billing.PromosResultSerialization
    val preser = new PromosResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[PromosResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue,Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Promos {

  implicit val formats = DefaultFormats
  
  private lazy val bucker = "promos"

  private lazy val riak = GWRiak(bucker)

  private val idxedBy = idxAccountsId

 /**
  * Only find by name is available now since promos are going to be added manually.
  * FindbyName is used to get the promo data
  */
   def findByName(name: String): ValidationNel[Throwable, Option[PromosResult]] = {
    InMemory[ValidationNel[Throwable, Option[PromosResult]]]({
      name: String =>
        {
          play.api.Logger.debug(("%-20s -->[%s]").format("InMemory", name))
          (riak.fetch(name) leftMap { t: NonEmptyList[Throwable] =>
            new ServiceUnavailableError(name, (t.list.map(m => m.getMessage)).mkString("\n"))
          }).toValidationNel.flatMap { xso: Option[GunnySack] =>
            xso match {
              case Some(xs) => {
                (Validation.fromTryCatchThrowable[models.billing.PromosResult,Throwable] {
                  parse(xs.value).extract[PromosResult]
                } leftMap { t: Throwable =>
                  new ResourceItemNotFound(name, t.getMessage)
                }).toValidationNel.flatMap { j: PromosResult =>
                  Validation.success[Throwable, Option[PromosResult]](j.some).toValidationNel
                }
              }
              case None => Validation.failure[Throwable, Option[PromosResult]](new ResourceItemNotFound(name, "")).toValidationNel
            }
          }
        }
    }).get(name).eval(InMemoryCache[ValidationNel[Throwable, Option[PromosResult]]]())

  }

   implicit val sedimentPromosName = new Sedimenter[ValidationNel[Throwable, Option[PromosResult]]] {
    def sediment(maybeASediment: ValidationNel[Throwable, Option[PromosResult]]): Boolean = {
      val notSed = maybeASediment.isSuccess
      notSed
    }
  }

}
