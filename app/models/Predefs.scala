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

import play.api._
import play.api.mvc._
import scalaz._
import Scalaz._
import net.liftweb.json._
import controllers.stack._
import com.stackmob.scaliak._
import com.basho.riak.client.query.indexes.{ RiakIndexes, IntIndex, BinIndex }
import com.basho.riak.client.http.util.{ Constants => RiakConstants }
import models._
import org.megam.common.riak.{ GSRiak, GunnySack }

/**
 * @author rajthilak
 *
 */
case class PredefsJSON(id: String, name: String, provider: String, role: String, packaging: String)

object Predefs extends Helper {

  implicit val formats = DefaultFormats
  private lazy val riak: GSRiak = GSRiak(MConfig.riakurl, "predefs")

  /**
   * The create calls and puts stuff in Riak if the predefs don't exists for the following
   * rails
   * riak
   * play
   * akka
   * redis
   * postgresql
   * java
   * nodejs
   * rabbitmq
   * Every key/value stored in riak has the the name "eg: rails, play" as the key, and an index named
   * predefName = "rails" as well.
   * TO-DO: We'll use memcache to store it, and retrieve it using StateMonad.
   */
  def create: List[Option[ValidationNel[Throwable, Option[GunnySack]]]] = {
    val metadataKey = "predef"
    val metadataVal = "predefs Creation"
    val bindex = BinIndex.named("predefName")
    for {
      predef_full <- List(riakJSON, nodejsJSON, playJSON, akkaJSON, redisJSON)
    } yield {
      val m = parse(predef_full).extract[PredefsJSON]
      val bvalue = Set(m.name)
      riak.store(new GunnySack(m.name, predef_full, RiakConstants.CTYPE_TEXT_UTF8, None, Map(metadataKey -> metadataVal), Map((bindex, bvalue))))
    }.some
  }

  /*
   * fetch the object using their key from bucket
   */
  def findByKey(key: String): ValidationNel[Error, Option[PredefsJSON]] = {
    riak.fetch(key) match {
      case Success(msg) => {
        val caseValue = msg.get
        val json = parse(caseValue.value)
        val m = json.extract[PredefsJSON]
        Validation.success[Error, Option[PredefsJSON]](Some(m)).toValidationNel
      }
      case Failure(err) => Validation.failure[Error, Option[PredefsJSON]](new Error("""
               |In this predef '%s' is doesn't exists in your list 
               |Please add predef's in your list.""".format(key).stripMargin + "\n ")).toValidationNel
    }
  }

  def listKeys: Validation[Throwable, Stream[String]] = riak.listKeys

  /**
   * Use this with caution. We don't want to call it always.The predefs will be memcached using a key.
   * TO-DO: We'll use StateMonad to perform Cache get/set.
   */
  def firstTimeLoad: List[Option[ValidationNel[Throwable, Option[GunnySack]]]] = {
    listKeys match {
      case xs #:: xs1 => models.Predefs.create
    }
  }

}