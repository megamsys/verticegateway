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
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import com.stackmob.scaliak._
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import org.megam.util.Time
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import com.basho.riak.client.core.query.indexes.{RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import models.cache._
import models.riak._
import controllers.funnel.FunnelErrors._
import controllers.Constants._
import controllers.stack.MConfig
import java.nio.charset.Charset


/**
 * @author rajthilak
 */

/*
case class CatRequestInput(cat_id: String, cattype: String, name: String, action: String, category: String) {
  val json = "\"cat_id\":\"" + cat_id + "\",\"cattype\":\"" + cattype + "\",\"name\":\"" + name + "\",\"action\":\"" + action + "\",\"category\":\"" + category + "\""
}
case class CatRequestResult(id: String, cat_id: String, cattype: String, name: String, action: String, category: String, created_at: String) {
  val json = "{\"id\": \"" + id + "\",\"cat_id\": \"" + cat_id + "\",\"cattype\":\"" + cattype + "\",\"name\":\"" + name + "\",\"action\":\"" + action + "\",\"category\":\"" + category + "\",\"created_at\":\"" + created_at + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.CatRequestResultSerialization
    val nrsser = new CatRequestResultSerialization()
    toJSON(this)(nrsser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object CatRequestResult {

  def apply = new CatRequestResult(new String(), new String(), new String(), new String(), new String(), new String(), new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[CatRequestResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.CatRequestResultSerialization
    val nrsser = new CatRequestResultSerialization()
    fromJSON(jValue)(nrsser.reader)
  }

  def fromJson(json: String): Result[CatRequestResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue,Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}


object CatRequests {

  implicit val formats = DefaultFormats

  private val riak = GWRiak( "catreqs")

  val metadataKey = "CatRequest"
  val metadataVal = "CatRequest Creation"
  val bindex = "appId"

  /**
   * A private method which chains computation to make GunnySack for existing node when provided with an input json, Option[node_name].
   * parses the json, and converts it to requestinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the GunnySack object is built.
   * If the node_name is send by the Node model. It then yield the GunnySack object.
   */
  private def mkGunnySack(input: String): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.CatRequests", "mkGunnySack:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("models.CatRequests:json", input))

    //Does this failure get propagated ? I mean, the input json parse fails ? I don't think so.
    //This is a potential bug.
    val ripNel: ValidationNel[Throwable, RequestInput] = (Validation.fromTryCatchThrowable[RequestInput,Throwable] {
      parse(input).extract[RequestInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    play.api.Logger.debug(("%-20s -->[%s]").format("models.CatRequests:rip", ripNel))
    for {
      rip <- ripNel
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "cat").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {

      val bvalue = Set(rip.cat_id)
      val json = "{\"id\": \"" + (uir.get._1 + uir.get._2) + "\"," + rip.json + ",\"created_at\":\"" + Time.now.toString + "\"}"
      new GunnySack((uir.get._1 + uir.get._2), json, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /*
   * create new CatRequest with the 'Nodename' of the node provide as input.
   * A index name appreqID will point to the "appreqs" bucket
   */
  def create(input: String): ValidationNel[Throwable, Option[Tuple3[Map[String,String], String, String]]] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.CatRequests", "create:Entry"))
    play.api.Logger.debug(("%-20s -->[%s]").format("json", input))

    (mkGunnySack(input) leftMap { err: NonEmptyList[Throwable] =>
      err
    }).flatMap { gs: Option[GunnySack] =>
      (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
        flatMap { maybeGS: Option[GunnySack] =>
          val req_result = parse(gs.get.value).extract[RequestResult]
          play.api.Logger.debug(("%-20s -->[%s]%nwith%n----%n%s").format("CatRequest.created successfully", "input", input))
          maybeGS match {
            case Some(thatGS) => Tuple3(Map[String,String](("Id" -> gs.get.key), ("Action" -> req_result.action), ("Category" -> req_result.category)), req_result.name, req_result.cattype).some.successNel[Throwable]
            case None => {
              play.api.Logger.warn(("%-20s -->[%s]").format("CatRequest.created success", "Scaliak returned => None. Thats OK."))
              Tuple3(Map[String,String](("Id" -> gs.get.key), ("Action" -> req_result.action), ("Category" -> req_result.category)), req_result.name, req_result.cattype).some.successNel[Throwable]
            }
          }
        }
    }
  }

}
*/