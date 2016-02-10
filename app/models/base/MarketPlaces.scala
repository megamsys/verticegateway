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
import com.twitter.util.{Future, Await}
import com.twitter.conversions.time._

import com.websudos.phantom.dsl._
import com.websudos.phantom.iteratee.Iteratee
import com.websudos.phantom.connectors.{ContactPoint, KeySpaceDef}



case class MarketPlaceSack (
  name: String
)


//table class for holding the ds of a particular type(mkp in our case)
sealed class MarketPlaceSacks extends CassandraTable[MarketPlaceSacks, MarketPlaceSack] {

  object name extends StringColumn(this)

 override def fromRow(r: Row): MarketPlaceSack = { MarketPlaceSack(name(r)) }


}

//Needs -> DS CLASS (MkpSacks)
//      -> Connector class
//This class talks to the cassandra and performs the actions
abstract class ConcreteMkp extends MarketPlaceSacks with RootConnector {

  override lazy val tableName = "mkpt"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def findAll(): ValidationNel[Throwable, List[MarketPlaceSack]] = {
  val resp =  select.collect()
   Await.result(resp, 5.second).successNel
 }

}




//case class MarketPlaceResult(id: String, name: String, catalog: MarketPlaceCatalog, features: MarketPlaceFeatures, plans: MarketPlacePlans, applinks: MarketPlaceAppLinks, attach: String, predefnode: String, approved: String, created_at: String) {
case class MarketPlaceResult(id: String, name: String, cattype: String, order: String, image: String, url: String, envs: models.tosca.KeyValueList, plans: MarketPlacePlans, created_at: String) {

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    import models.json.MarketPlaceResultSerialization
    val preser = new MarketPlaceResultSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }
}

object MarketPlaceResult {

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[MarketPlaceResult] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON

    import models.json.MarketPlaceResultSerialization
    val preser = new MarketPlaceResultSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[MarketPlaceResult] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}




class Marketplaces extends ConcreteMkp {


 def listAll(): ValidationNel[Throwable, List[MarketPlaceSack]] = {
   for {
     mkp <- findAll()
   } yield {
       mkp }
 }
}
