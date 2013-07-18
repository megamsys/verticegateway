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
case class PredefsInput(id: String = new String(), name: String, provider: String, provider_role: String, build_monkey: String = "none") {
  val json = "{\"id\": \"" + id + "\",\"name\":\"" + name + "\",\"prov\":\"" + provider + "\",\"provider_role\":\"" + provider_role + "\",\"build_monkey\":\"" + build_monkey + "\"}"
}

object PredefsInput {

  val toMap = Map[String, PredefsInput](
    "akka" -> PredefsInput("", "akka", "chef", "akka", "sbt"),
    "java" -> PredefsInput("", "java", "chef", "java", "mvn"),
    "nodejs" -> PredefsInput("", "nodejs", "chef", "nodejs", "npm"),
    "play" -> PredefsInput("play", "chef", "play", "sbt"),
    "postgresql" -> PredefsInput("postgresql", "postgresql", "chef", "postgresql"),
    "rails" -> PredefsInput("rails", "chef", "rails", "bundle"),
    "rabbitmq" -> PredefsInput("rabbitmq", "rabbitmq", "chef", "riak"),
    "redis" -> PredefsInput("", "redis", "chef", "riak"),
    "riak" -> PredefsInput("", "riak", "chef", "riak"))

}

case class PredefsResult(id: String, name: String, provider: String, provider_role: String, build_monkey: String)

object Predefs {

  implicit val formats = DefaultFormats
  implicit def PredefsResultsSemigroup: Semigroup[PredefsResults] = Semigroup.instance((f1, f2) => f1.append(f2))

  private lazy val riak: GSRiak = GSRiak(MConfig.riakurl, "predefs")

  val metadataKey = "Predef"
  val metadataVal = "Predefs Creation"
  val bindex = BinIndex.named("predefName")
  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * If the unique id is got successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(input: PredefsInput): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug("models.Predefs mkGunnySack: entry:\n" + input.json)
    val predefInput: ValidationNel[Throwable, PredefsInput] = (Validation.fromTryCatch {
      parse(input.json).extract[PredefsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input.json, t.getMessage) }).toValidationNel //capture failure
    
    for {
      pip <- predefInput
      //TO-DO: Does the leftMap make sense ? To check during function testing, by removing it.
      uir <- (UID(MConfig.snowflakeHost, MConfig.snowflakePort, "pre").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      //TO-DO: do we need a match for uir to filter the None case. confirm it during function testing.
      play.api.Logger.debug("models.Predefs mkGunnySack: yield:\n" + (uir.get._1 + uir.get._2))
      val bvalue = Set(pip.name)
      val pipJson = new PredefsInput((uir.get._1 + uir.get._2), pip.name, pip.provider, pip.provider_role, pip.build_monkey).json
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
  def create: ValidationNel[Throwable, PredefsResults] = {
    play.api.Logger.debug("models.Predefs create: entry")
    (PredefsInput.toMap.some map {
      _.map { p =>
        (mkGunnySack(p._2) leftMap { err: NonEmptyList[Throwable] =>          
          new ServiceUnavailableError(p._2.json, (err.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { gs: Option[GunnySack] =>
          (riak.store(gs.get) leftMap { t: NonEmptyList[Throwable] => t }).
            flatMap { maybeGS: Option[GunnySack] =>
              maybeGS match {
                case Some(thatGS) => (Validation.success[Throwable, PredefsResults](PredefsResults(parse(thatGS.value).extract[PredefsResult]))).toValidationNel //screwy kishore, every element in a list ? 
                case None         => (Validation.failure[Throwable, PredefsResults](new ResourceItemNotFound(p._2.json, "The predef wasn't created, store failed:'"))).toValidationNel
              }
            }
        }
      }
    } map {
      _.foldRight((PredefsResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head.  

  }

  /*
   * Uses the StateMonad to store the first time fetch of a predef in a cache where the stale gets invalidated every 5 mins.
   * The downside of this approach is any new update will have to wait 5 mins
   * Every time a State is run, a new InMemoryCache is used as the initial state. This is ok, as we use InMemoryCache as a fascade 
   * to the actualy play.api.cache.Cache object.
   */
  def findByName(name: String): ValidationNel[Error, Option[PredefsResult]] = {
    Logger.debug("models.Predefs findByName: entry:" + name)
    InMemory[ValidationNel[Error, Option[PredefsResult]]]({
      name: String =>
        {
          Logger.debug("models.Predefs findByName: InMemory:" + name)
          (riak.fetch(name) leftMap { t: NonEmptyList[Throwable] =>
            new ServiceUnavailableError(name, (t.list.map(m => m.getMessage)).mkString("\n"))
          }).toValidationNel.flatMap { xso: Option[GunnySack] =>
            xso match {
              case Some(xs) => {
                (Validation.fromTryCatch {
                  parse(xs.value).extract[PredefsResult]
                } leftMap { t: Throwable =>
                  new ResourceItemNotFound(name, t.getMessage)
                }).toValidationNel.flatMap { j: PredefsResult =>
                  Validation.success[Error, Option[PredefsResult]](j.some).toValidationNel
                }
              }
              case None => Validation.failure[Error, Option[PredefsResult]](new ResourceItemNotFound(name, "Please rerun predef initiation again.")).toValidationNel
            }
          }
        }
    }).get(name).eval(InMemoryCache[ValidationNel[Error, Option[PredefsResult]]]())
  }

  def listKeys: ValidationNel[Throwable, Stream[String]] = riak.listKeys.toValidationNel

}