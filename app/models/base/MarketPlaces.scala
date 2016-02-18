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
import com.twitter.util.{ Future, Await }
import com.twitter.conversions.time._

import com.websudos.phantom.dsl._
import com.websudos.phantom.iteratee.Iteratee
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }

/**
 *
 * @author morpheyesh
 */


case class MarketPlaceSack(
  settings_name: String,
  cattype: String,
  flavor: String,
  image: String,
  url: String,
  envs: Map[String, String],
  plans: Map[String, String]) {}



//table class for holding the ds of a particular type(mkp in our case)
sealed class MarketPlaceT extends CassandraTable[MarketPlaceT, MarketPlaceSack] {

  object settings_name extends StringColumn(this)
  object cattype extends StringColumn(this)
  object flavor extends StringColumn(this) with PrimaryKey[String]
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
 *   This class talks to scylla and performs the actions
 */
abstract class ConcreteMkp extends MarketPlaceT with ScyllaConnector {

  override lazy val tableName = "mkplaces"

}

object MarketPlaces extends ConcreteMkp {

  def listAll(): ValidationNel[Throwable, Seq[MarketPlaceSack]] = {
    val resp = select.collect()
    (Await.result(resp, 5.second)).successNel
  }

  def findByName(flavor: String): ValidationNel[Throwable, MarketPlaceSack] = {

    val resp = select.allowFiltering().where(_.flavor eqs flavor).get()
    (Await.result(resp, 5.second)).get.successNel
  }
}
