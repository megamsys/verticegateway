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
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._
import app.MConfig

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._
import io.megam.common.uid.UID
import io.megam.util.Time

import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author rajthilak
 *
 */

case class AvailableunitsInput(name: String, duration: String, charges_per_duration: String) {
  val json = "{\"name\":\"" + name + "\",\"duration\":\"" + duration + "\",\"charges_per_duration\":\"" + charges_per_duration + "\"}"

}

case class AvailableunitsResult(
    id: String,
    name: String,
    duration: String,
    charges_per_duration: String,
    json_claz: String,
    created_at: String) {
}

sealed class AvailableunitsSacks extends CassandraTable[AvailableunitsSacks, AvailableunitsResult] {

  implicit val formats = DefaultFormats

  object id extends StringColumn(this) with PrimaryKey[String]
  object name extends StringColumn(this)
  object duration extends StringColumn(this)
  object charges_per_duration extends StringColumn(this)
  object json_claz extends StringColumn(this)
  object created_at extends StringColumn(this)

  def fromRow(row: Row): AvailableunitsResult = {
    AvailableunitsResult(
      id(row),
      name(row),
      duration(row),
      charges_per_duration(row),
      json_claz(row),
      created_at(row))
  }
}

abstract class ConcreteAvailableunits extends AvailableunitsSacks with RootConnector {
  // you can even rename the table in the schema to whatever you like.
  override lazy val tableName = "availableunits"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(ams: AvailableunitsResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, ams.id)
      .value(_.name, ams.name)
      .value(_.duration, ams.duration)
      .value(_.charges_per_duration, ams.charges_per_duration)
      .value(_.json_claz, ams.json_claz)
      .value(_.created_at, ams.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

}

object Availableunits extends ConcreteAvailableunits{

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to eventsinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkAvailableunitsSack(email: String, input: String): ValidationNel[Throwable, AvailableunitsResult] = {
    val AvailableunitsInput: ValidationNel[Throwable, AvailableunitsInput] = (Validation.fromTryCatchThrowable[AvailableunitsInput, Throwable] {
      parse(input).extract[AvailableunitsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      aui <- AvailableunitsInput
      uir <- (UID("uts").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val json = new AvailableunitsResult(uir.get._1 + uir.get._2, aui.name, aui.duration, aui.charges_per_duration, "Megam::Availableunits", Time.now.toString)
      json
    }
  }

  /*
   * create new static units for seperate item with the 'name' of the item provide as input.
   * Also creating index with 'static units'
   */

  def create(email: String, input: String): ValidationNel[Throwable, Option[AvailableunitsResult]] = {
    for {
      wa <- (mkAvailableunitsSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Availableunits.created success", Console.RESET))
      wa.some
    }
  }

}
