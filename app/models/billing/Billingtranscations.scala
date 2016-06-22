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

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._

import io.megam.util.Time
import app.MConfig
import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author rajthilak
 *
 */

case class BillingtranscationsInput(gateway: String, amountin: String, amountout: String, fees: String, tranid: String, trandate: String, currency_type: String) {

}

case class BillingtranscationsResult(
    id: String,
    account_id: String,
    gateway: String,
    amountin: String,
    amountout: String,
    fees: String,
    tranid: String,
    trandate: String,
    currency_type: String,
    json_claz: String,
    created_at: String) {
}

sealed class BillingtranscationsSacks extends CassandraTable[BillingtranscationsSacks, BillingtranscationsResult] {

  implicit val formats = DefaultFormats

  object id extends StringColumn(this)
  object account_id extends StringColumn(this) with PartitionKey[String]
  object gateway extends StringColumn(this)
  object amountin extends StringColumn(this)
  object amountout extends StringColumn(this)
  object fees extends StringColumn(this)
  object tranid extends StringColumn(this)
  object trandate extends StringColumn(this) with PrimaryKey[String]
  object currency_type extends StringColumn(this)
  object json_claz extends StringColumn(this)
  object created_at extends StringColumn(this) with PrimaryKey[String]

  def fromRow(row: Row): BillingtranscationsResult = {
    BillingtranscationsResult(
      id(row),
      account_id(row),
      gateway(row),
      amountin(row),
      amountout(row),
      fees(row),
      tranid(row),
      trandate(row),
      currency_type(row),
      json_claz(row),
      created_at(row))
  }
}

abstract class ConcreteBillingtranscations extends BillingtranscationsSacks with RootConnector {
  // you can even rename the table in the schema to whatever you like.
  override lazy val tableName = "billingtranscations"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(ams: BillingtranscationsResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, ams.id)
      .value(_.account_id, ams.account_id)
      .value(_.gateway, ams.gateway)
      .value(_.amountin, ams.amountin)
      .value(_.amountout, ams.amountout)
      .value(_.fees, ams.fees)
      .value(_.tranid, ams.tranid)
      .value(_.trandate, ams.trandate)
      .value(_.currency_type, ams.currency_type)
      .value(_.json_claz, ams.json_claz)
      .value(_.created_at, ams.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(id: String): ValidationNel[Throwable, Seq[BillingtranscationsResult]] = {
    val res = select.allowFiltering().where(_.account_id eqs id).limit(20).fetch()
    Await.result(res, 5.seconds).successNel
  }

}

object Billingtranscations extends ConcreteBillingtranscations {

  /**
   * A private method which chains computation to make GunnySack when provided with an input json, email.
   * parses the json, and converts it to eventsinput, if there is an error during parsing, a MalformedBodyError is sent back.
   * After that flatMap on its success and the account id information is looked up.
   * If the account id is looked up successfully, then yield the GunnySack object.
   */
  private def mkBillingtranscationsSack(email: String, input: String): ValidationNel[Throwable, BillingtranscationsResult] = {
    val billInput: ValidationNel[Throwable, BillingtranscationsInput] = (Validation.fromTryCatchThrowable[BillingtranscationsInput, Throwable] {
      parse(input).extract[BillingtranscationsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      bill <- billInput
      uir <- (UID("bhs").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {

      val bvalue = Set(email)
      val json = new BillingtranscationsResult(uir.get._1 + uir.get._2, email, bill.gateway, bill.amountin, bill.amountout, bill.fees, bill.tranid, bill.trandate, bill.currency_type, "Megam::Billingtranscations", Time.now.toString)
      json
    }
  }

  /*
   * create new billing transcations for currently pay the bill of user.
   *
   */
  def create(email: String, input: String): ValidationNel[Throwable, Option[BillingtranscationsResult]] = {
    for {
      wa <- (mkBillingtranscationsSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Billingtranscations.created success", Console.RESET))
      wa.some
    }
  }

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email,
   * the transcations are listed on the index (account.id) in bucket `Billingtranscations`.
   * Using a "Billingtranscations name" as key, r      wa <- (msSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
eturn a list of ValidationNel[List[BillinghistoriesResult]]
   * Takes an email, and returns a Future[ValidationNel, List[Option[BillingtranscationsResult]]]
   */
  def findByEmail(email: String): ValidationNel[Throwable, Seq[BillingtranscationsResult]] = {
    (listRecords(email) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(email, "Billingtranscations = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[BillingtranscationsResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[BillingtranscationsResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[BillingtranscationsResult]](new ResourceItemNotFound(email, "Billingtranscations = nothing found.")).toValidationNel
    }

  }

}
