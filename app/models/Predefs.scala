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
package models

import scalaz._
import scalaz.Semigroup
import scalaz.syntax.SemigroupOps
import scalaz.NonEmptyList._
import scalaz.Validation
import scalaz.Validation._
import Scalaz._
import play.api._
import play.api.mvc._
import net.liftweb.json._
import models._
import models.cache.{ InMemory, InMemoryCache }
import controllers.funnel.FunnelErrors._
import controllers.stack._
import com.stackmob.scaliak._
import com.basho.riak.client.query.indexes.{ RiakIndexes, IntIndex, BinIndex }
import com.basho.riak.client.http.util.{ Constants => RiakConstants }
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID

/**
 * @author rajthilak
 *
 */
case class PredefInput(id: String = new String(), name: String, provider: String, provider_role: String, build_monkey: String = "none") {
  val json = "{\"id\": \"" + id + "\",\"name\":\"" + name + "\",\"prov\":\"" + provider + "\",\"provider_role\":\"" + provider_role + "\",\"build_monkey\":\"" + build_monkey + "\"}"
}

object PredefInput {

  val toMap = Map[String, PredefInput](
    "akka" -> PredefInput("", "akka", "chef", "akka", "sbt"),
    "java" -> PredefInput("", "java", "chef", "java", "mvn"),
    "nodejs" -> PredefInput("", "nodejs", "chef", "nodejs", "npm"),
    "play" -> PredefInput("play", "chef", "play", "sbt"),
    "postgresql" -> PredefInput("postgresql", "postgresql", "chef", "postgresql"),
    "rails" -> PredefInput("rails", "chef", "rails", "bundle"),
    "rabbitmq" -> PredefInput("rabbitmq", "rabbitmq", "chef", "riak"),
    "redis" -> PredefInput("", "redis", "chef", "riak"),
    "riak" -> PredefInput("", "riak", "chef", "riak"))

}

case class PredefResult(id: String, name: String, provider: String, provider_role: String, build_monkey: String)

object Predefs {

  implicit val formats = DefaultFormats
  implicit def PredefResultsSemigroup: Semigroup[PredefResults] = Semigroup.instance((f1, f2) => f1.append(f2))

  private lazy val riak: GSRiak = GSRiak(MConfig.riakurl, "predefs")

  val metadataKey = "Predef"
  val metadataVal = "Predefs Creation"
  val bindex = BinIndex.named("predefName")
  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * If the unique id is got successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(input: PredefInput): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug("models.Predefs mkGunnySack: entry:\n" + input.json)
    val predefInput: ValidationNel[Throwable, PredefInput] = (Validation.fromTryCatch {
      parse(input.json).extract[PredefInput]
    } leftMap { t: Throwable => new MalformedBodyError(input.json, t.getMessage) }).toValidationNel //capture failure

    for {
      pip <- predefInput
      //TO-DO: Does the leftMap make sense ? To check during function testing, by removing it.
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "pre").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for uir to filter the None case. confirm it during function testing.
      play.api.Logger.debug("models.Predefs mkGunnySack: yield:\n" + (uir.get._1 + uir.get._2))
      val bvalue = Set(pip.name)
      val pipJson = new PredefInput((uir.get._1 + uir.get._2), pip.name, pip.provider, pip.provider_role, pip.build_monkey).json
      new GunnySack(pip.name, pipJson, RiakConstants.CTYPE_TEXT_UTF8, None,
        Map(metadataKey -> metadataVal), Map((bindex, bvalue))).some
    }
  }

  /**
   * The create calls and puts stuff in Riak if the predefs don't exists for the following
   * akka, java, nodejs, play, postgresql, rails, riak, rabbitmq, redis
   * Every key/value stored in riak has the the name "eg: rails, play" as the key, and an index named
   * predefName = "rails" as well.
   */
  def create: ValidationNel[Throwable, PredefResults] = {
    play.api.Logger.debug("models.Predefs create: entry")
    (PredefInput.toMap.some map {
      _.map { p =>
        (mkGunnySack(p._2) leftMap { t: NonEmptyList[Throwable] => t
        }).flatMap { gs: Option[GunnySack] =>
          (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
            flatMap { maybeGS: Option[GunnySack] =>
              maybeGS match {
                case Some(thatGS) => (Validation.success[Throwable, PredefResults](PredefResults(parse(thatGS.value).extract[PredefResult]))).toValidationNel //screwy kishore, every element in a list ? 
                case None         => (Validation.failure[Throwable, PredefResults](new ResourceItemNotFound(p._2.json, "The predef wasn't created, store failed:'"))).toValidationNel
              }
            }
        }
      }
    } map {
      _.fold((PredefResults.empty).successNel[Throwable])(_ +++ _) //fold or foldRight ? 
    }).head //return the folded element in the head.  

  }

  /*
   * Uses the StateMonad to store the first time fetch of a predef in a cache where the stale gets invalidated every 5 mins.
   * The downside of this approach is any new update will have to wait 5 mins
   * Every time a State is run, a new InMemoryCache is used as the initial state. This is ok, as we use InMemoryCache as a fascade 
   * to the actualy play.api.cache.Cache object.
   */
  def findByName(name: String): ValidationNel[Error, Option[PredefResult]] = {
    Logger.debug("models.Predefs findByName: entry:" + name)
    InMemory[ValidationNel[Error, Option[PredefResult]]]({
      name: String =>
        {
          Logger.debug("models.Predefs findByName: InMemory:" + name)
          (riak.fetch(name) leftMap { t: NonEmptyList[Throwable] =>
            new ServiceUnavailableError(name, (t.list.map(m => m.getMessage)).mkString("\n"))
          }).toValidationNel.flatMap { xso: Option[GunnySack] =>
            xso match {
              case Some(xs) => {
                (Validation.fromTryCatch {
                  parse(xs.value).extract[PredefResult]
                } leftMap { t: Throwable =>
                  new ResourceItemNotFound(name, t.getMessage)
                }).toValidationNel.flatMap { j: PredefResult =>
                  Validation.success[Error, Option[PredefResult]](j.some).toValidationNel
                }
              }
              case None => Validation.failure[Error, Option[PredefResult]](new ResourceItemNotFound(name, "Please rerun predef initiation again.")).toValidationNel
            }
          }
        }
    }).get(name).eval(InMemoryCache[ValidationNel[Error, Option[PredefResult]]]())
  }

  def listKeys: ValidationNel[Throwable, Stream[String]] = riak.listKeys.toValidationNel

}