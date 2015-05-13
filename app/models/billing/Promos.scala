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

package models.billing

import scalaz._
import scalaz.syntax.SemigroupOps
import scalaz.NonEmptyList._
import scalaz.Validation._
import scalaz.effect.IO
import scalaz.EitherT._
import org.megam.util.Time
import Scalaz._
import controllers.stack._
import controllers.Constants._
import controllers.funnel.FunnelErrors._
import models._
import models.billing._
import models.cache._
import models.riak._
import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author morpheyesh
 *
 */

//case class PromosInput(code: String) {
//  val json = "{\"code\":\"" + code + "\"}"

//}


case class PromosResult(id: String, code: String, amount: String, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.billing.PromosResultSerialization
    val preser = new PromosResultSerialization()
    toJSON(this)(preser.writer) //where does this JSON from? 
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
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

  def fromJson(json: String): Result[PromosResult] = (Validation.fromTryCatch {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Promos {
  
  
  implicit val formats = DefaultFormats
  private val riak = GWRiak("promos")

  
  
  val metadataKey = "Promos"
  val metadataVal = "Promos Creation"
  val bindex = "promos"

 /**
  * Only find by name is available now since promos are going to be added manually.
  * FindbyName is used to get the promo data
  */


   def findByName(name: String): ValidationNel[Throwable, Option[PromosResult]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.billings.Promos", "findByName:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("findByName", name))
    InMemory[ValidationNel[Throwable, Option[PromosResult]]]({
      name: String =>
        {
          play.api.Logger.debug(("%-20s -->[%s]").format("InMemory", name))
          (riak.fetch(name) leftMap { t: NonEmptyList[Throwable] =>
            new ServiceUnavailableError(name, (t.list.map(m => m.getMessage)).mkString("\n"))
          }).toValidationNel.flatMap { xso: Option[GunnySack] =>
            xso match {
              case Some(xs) => {
                (Validation.fromTryCatch[models.billing.PromosResult] {
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
      play.api.Logger.debug("%-20s -->[%s]".format("|^/^|-->ACT:sediment:", notSed))
      notSed
    }
  }
  
}

