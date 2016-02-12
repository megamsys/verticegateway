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
package models.base

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import models.json._
import models.base._
import db._
import cache._
import app.MConfig
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._
import scalaz.Validation
import scalaz.Validation.FlatMap._

import com.stackmob.scaliak._
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }
import io.megam.common.riak.GunnySack
import io.megam.util.Time
import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
//import scala.concurrent.{Await, Future}
import com.twitter.util.{ Future, Await }
import com.twitter.conversions.time._

import com.websudos.phantom.dsl._
import com.websudos.phantom.iteratee.Iteratee
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }

case class MarketPlaceSack(
  settings_name: String,
  cattype: String,
  flavor: String,
  image: String,
  url: String,
  envs: Map[String, String],
  plans: Map[String, String]) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.MarketPlaceSackSerialization
    val preser = new MarketPlaceSackSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object MarketPlaceSack {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[MarketPlaceSack] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON

    import models.json.MarketPlaceSackSerialization
    val preser = new MarketPlaceSackSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[MarketPlaceSack] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

//table class for holding the ds of a particular type(mkp in our case)
sealed class MarketPlaceT extends CassandraTable[MarketPlaceT, MarketPlaceSack] {

  object settings_name extends StringColumn(this)
  object cattype extends StringColumn(this)
  object flavor extends StringColumn(this)
  object image extends StringColumn(this)
  object url extends StringColumn(this)
  object envs extends MapColumn[MarketPlaceT, MarketPlaceSack, String, String](this)
  object plans extends MapColumn[MarketPlaceT, MarketPlaceSack, String, String](this)

  override def fromRow(r: Row): MarketPlaceSack = {
    MarketPlaceSack(
      settings_name(r),
      cattype(r),
      flavor(r),
      image(r),
      url(r),
      envs(r),
      plans(r))
  }
}

/*
 *   This class talks to the cassandra and performs the actions
 */
abstract class ConcreteMkp extends MarketPlaceT with RootConnector {

  override lazy val tableName = "mkplaces"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def findAll(): ValidationNel[Throwable, MarketPlaceSacks] = {
    val resp = select.collect()

    val p = (Await.result(resp, 5.second)) map { i: MarketPlaceSack => (i.some) }
    return Validation.success[Throwable, MarketPlaceSacks](nel(p.head, p.tail)).toValidationNel
  }
}

object MarketPlaces extends ConcreteMkp {

  def listAll(): ValidationNel[Throwable, MarketPlaceSacks] = {
    for {
      mkp <- findAll()
    } yield {
      mkp
    }
  }
}
