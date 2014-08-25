/* 
** Copyright [2013-2014] [Megam Systems]
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
//import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps
import models._
import models.cache._
import models.riak._
import org.megam.util.Time
import controllers.funnel.FunnelErrors._
import controllers.stack._
import controllers.Constants._
import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import org.megam.common.riak.{ GSRiak, GunnySack }
import org.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz.{ Result, UncategorizedError }
import java.nio.charset.Charset



/**
 * @author rajthilak
 *
 */
case class PredefInput(id: String = new String(), name: String, provider: String, provider_role: String, build_monkey: String = "none", runtime_exec: String, created_at: String) {
  val json = "{\"id\": \"" + id + "\",\"name\":\"" + name + "\",\"provider\":\"" + provider + "\",\"provider_role\":\"" + provider_role + "\",\"build_monkey\":\"" + build_monkey + "\",\"runtime_exec\":\"" + runtime_exec + "\",\"created_at\":\"" + created_at + "\"}"     
}

object PredefInput {

  val toMap = Map[String, PredefInput](
    "rails" -> PredefInput("", "rails", "chef", "rails", "bundle", "#[start] unicorn_<projectname>", ""),
    "java" -> PredefInput("", "java", "chef", "java", "mvn", "#[start] tomcat", ""),
    "scala" -> PredefInput("", "scala", "chef", "scala", "sbt", "", ""),
    "play" -> PredefInput("", "play", "chef", "play", "sbt", "#[start] play", ""),
    "akka" -> PredefInput("", "akka", "chef", "scala", "sbt", "#[start] akka", ""),
    "nodejs" -> PredefInput("", "nodejs", "chef", "nodejs", "npm", "#[start] nodejs", ""),
    "mobhtml5" -> PredefInput("", "mobhtml5", "chef", "sencha", "", "service nginx restart", ""),
    "postgresql" -> PredefInput("", "postgresql", "chef", "postgresql", "gulpd", "service postgresql restart", ""),
    "rabbitmq" -> PredefInput("", "rabbitmq", "chef", "rabbitmq", "gulpd", "rabbitmqctl start_app", ""),
    "redis" -> PredefInput("", "redis", "chef", "redis", "gulpd", "redis-server", ""),
    "riak" -> PredefInput("", "riak", "chef", "riak", "gulpd", "#[start] riak", ""),
    "wordpress" -> PredefInput("", "wordpress", "chef", "wordpress", "", "#[start] wordpress", ""),
    "joomla" -> PredefInput("", "joomla", "chef", "joomla", "", "#[start] joomla", ""),
    "jenkins" -> PredefInput("", "jenkins", "chef", "jenkins", "", "#[start] jenkins", ""),
    "hadoop" -> PredefInput("", "hadoop", "chef", "hadoop", "", "#[start] hadoop", ""),
    "scmmanager" -> PredefInput("", "scmmanager", "chef", "scmmanager", "", "#[start] scmmanager", ""),
    "orion" -> PredefInput("", "orion", "chef", "orion", "", "#[start] orion", ""),
    "otrs" -> PredefInput("", "otrs", "chef", "otrs", "", "#[start] otrs", ""),
    "redmine" -> PredefInput("", "redmine", "chef", "redmine", "", "#[start] redmine", ""),
    "liferay" -> PredefInput("", "liferay", "chef", "liferay", "", "#[start] liferay", ""),
    "sugercrm" -> PredefInput("", "sugercrm", "chef", "sugercrm", "", "#[start] sugercrm", ""),
    "op5" -> PredefInput("", "op5", "chef", "op5", "", "#[start] op5", ""),
    "zarafa" -> PredefInput("", "zarafa", "chef", "zarafa", "", "service zarafa-server", "")
  )

  val toStream = toMap.keySet.toStream

}

case class PredefResult(id: String, name: String, provider: String, provider_role: String, build_monkey: String, runtime_exec: String, created_at: String) {
  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.PredefResultSerialization
    val preser = new PredefResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    pretty(render(toJValue))
  } else {
    compactRender(toJValue)
  }
}

object PredefResult {

  def apply(name: String): PredefResult = new PredefResult("not found", name, new String(), new String(), new String(), new String(), new String())

  def apply = new PredefResult(new String(), new String(), new String(), new String(), new String(), new String(), new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[PredefResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    import models.json.PredefResultSerialization
    val preser = new PredefResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[PredefResult] = (Validation.fromTryCatch[net.liftweb.json.JValue] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

object Predefs {

  implicit val formats = DefaultFormats
  implicit def PredefResultsSemigroup: Semigroup[PredefResults] = Semigroup.instance((f1, f2) => f1.append(f2))

  private val riak = GWRiak( "predefs")

  val metadataKey = "Predef"
  val metadataVal = "Predefs Creation"
  val bindex = "predefName"
  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to nodeinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * If the unique id is got successfully, then yield the GunnySack object.
   */
  private def mkGunnySack(input: PredefInput): ValidationNel[Throwable, Option[GunnySack]] = {
    play.api.Logger.debug("models.Predefs mkGunnySack: entry:\n" + input.json)
    val predefInput: ValidationNel[Throwable, PredefInput] = (Validation.fromTryCatch[models.PredefInput] {
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
      val pipJson = new PredefInput((uir.get._1 + uir.get._2), pip.name, pip.provider, pip.provider_role, pip.build_monkey, pip.runtime_exec, Time.now.toString).json
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
                case Some(thatGS) => PredefResults(parse(thatGS.value).extract[PredefResult]).successNel[Throwable]
                case None => {
                  play.api.Logger.warn(("%-20s -->[%s]").format("Predefs.created success", "Scaliak returned => None. Thats OK."))
                  PredefResults(PredefResult(new String(), p._2.name, p._2.provider,
                    p._2.provider_role, p._2.build_monkey, new String(), new String())).successNel[Throwable]
                }
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
  def findByName(predefsList: Option[Stream[String]]): ValidationNel[Throwable, PredefResults] = {
    play.api.Logger.debug("models.Predefs findByName: entry:")
    play.api.Logger.debug(("%-20s -->[%s]").format("predefsList", predefsList))
    (predefsList map {
      _.map { name =>
        InMemory[ValidationNel[Error, PredefResults]]({
          cname: String =>
            {
              play.api.Logger.debug("models.Predefs findByName: InMemory:" + cname)
              (riak.fetch(name) leftMap { t: NonEmptyList[Throwable] =>
                new ServiceUnavailableError(cname, (t.list.map(m => m.getMessage)).mkString("\n"))
              }).toValidationNel.flatMap { xso: Option[GunnySack] =>
                xso match {
                  case Some(xs) => {
                    (Validation.fromTryCatch[models.PredefResult] {
                      parse(xs.value).extract[PredefResult]
                    } leftMap { t: Throwable =>
                      new ResourceItemNotFound(cname, t.getMessage)
                    }).toValidationNel.flatMap { j: PredefResult =>
                      Validation.success[Error, PredefResults](nels(j.some)).toValidationNel
                    }
                  }
                  case None => Validation.failure[Error, PredefResults](new ResourceItemNotFound(cname, "Please rerun predef initiation again.")).toValidationNel
                }
              }
            }
        }).get(name).eval(InMemoryCache[ValidationNel[Error, PredefResults]]())
      }
    } map {
      _.fold((PredefResults.empty).successNel[Error])(_ +++ _)
    }).head //return the folded element in the head
  }

  def listAll: ValidationNel[Throwable, PredefResults] = {
    play.api.Logger.debug(("%-20s -->[%s]").format("models.Node", "listAll:Entry"))
    findByName(PredefInput.toStream.some) //return the folded element in the head.  
  }

  implicit val sedimentPredefResults = new Sedimenter[ValidationNel[Error, PredefResults]] {
    def sediment(maybeASediment: ValidationNel[Error, PredefResults]): Boolean = {
      val notSed = maybeASediment.isSuccess
      play.api.Logger.debug("%-20s -->[%s]".format("|^/^|-->PRC:sediment:", notSed))
      notSed
    }
  }
}