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

import cache._
import db._
import io.megam.auth.funnel.FunnelErrors._
import controllers.Constants._

import com.stackmob.scaliak._
import io.megam.auth.stack.AccountResult
import io.megam.common.riak.GunnySack
import io.megam.common.uid.UID
import io.megam.util.Time
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import com.basho.riak.client.core.query.indexes.{ RiakIndexes, StringBinIndex, LongIntIndex }
import com.basho.riak.client.core.util.{ Constants => RiakConstants }

import java.util.UUID
import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * @author rajthilak
 * authority
 *
 */
case class AccountSack(
  id: String,
  first_name: String,
  last_name: String,
  phone: String,
  email: String,
  api_key: String,
  password: String,
  authority: String,
  password_reset_key: String,
  password_reset_sent_at: String,
  created_at: String)

sealed class AccountSacks extends CassandraTable[AccountSacks, AccountResult] {
  //object id extends  UUIDColumn(this) with PartitionKey[UUID] {
  //  override lazy val name = "id"
  //}
  object id extends StringColumn(this)
  object first_name extends StringColumn(this)
  object last_name extends StringColumn(this)
  object phone extends StringColumn(this)
  object email extends StringColumn(this)
  object api_key extends StringColumn(this)
  object password extends StringColumn(this)
  object authority extends StringColumn(this)
  object password_reset_key extends StringColumn(this)
  object password_reset_sent_at extends StringColumn(this)
  object created_at extends StringColumn(this)

  def fromRow(row: Row): AccountResult = {
    AccountResult(
      id(row),
      first_name(row),
      last_name(row),
      phone(row),
      email(row),
      api_key(row),
      password(row),
      authority(row),
      password_reset_key(row),
      password_reset_sent_at(row),
      created_at(row))
  }
}

abstract class ConcreteAccounts extends AccountSacks with RootConnector {
  // you can even rename the table in the schema to whatever you like.
  override lazy val tableName = "accounts"

  def insertNewRecord(account: AccountResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, account.id)
      .value(_.first_name, account.first_name)
      .value(_.last_name, account.last_name)
      .value(_.phone, account.phone)
      .value(_.email, account.email)
      .value(_.api_key, account.api_key)
      .value(_.password, account.password)
      .value(_.authority, account.authority)
      .value(_.password_reset_key, account.password_reset_key)
      .value(_.password_reset_sent_at, account.password_reset_sent_at)
      .value(_.created_at, account.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  // def getRecipeById(id: UUID): ScalaFuture[Option[AccountInput]] = {
  //   select.where(_.id eqs id).one()
  // }

  //def deleteRecipeById(id: UUID): ScalaFuture[ResultSet] = {
  //   delete.where(_.id eqs id).future()
  //}
}

class AccountsDatabase(override val connector: KeySpaceDef) extends Database(connector) {

  object AccountSacks extends ConcreteAccounts with connector.Connector
  implicit val formats = DefaultFormats

  private def accountNel(input: String): ValidationNel[Throwable, AccountResult] = {
    (Validation.fromTryCatchThrowable[AccountResult, Throwable] {
      parse(input).extract[AccountResult]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
  }

  def create(input: String): ValidationNel[Throwable, ResultSet] = {
    for {
      acc <- accountNel(input)
      insert <- AccountSacks.insertNewRecord(acc)
    } yield {
      insert
    }
  }
}

//object AccountsDatabase extends AccountsDatabase(ContactPoint.local.keySpace("accounts"))

object Accounts {

  private val riak = GWRiak("accounts")
  implicit val formats = DefaultFormats

  //def create(input: String): ValidationNel[Throwable, Option[ResultSet]] = {
  // for {
  //acc <- parse(input).extract[AccountSack]
  //  insert <- AccountSacks.insertNewRecord(acc)
  // } yield insert.some.successNel[Throwable]
  // }

  def findByEmail(email: String): ValidationNel[Throwable, Option[AccountResult]] = {
    InMemory[ValidationNel[Throwable, Option[AccountResult]]]({
      name: String =>
        {
          play.api.Logger.debug(("%-20s -->[%s]").format("InMemory", email))
          (riak.fetch(email) leftMap { t: NonEmptyList[Throwable] =>
            new ServiceUnavailableError(email, (t.list.map(m => m.getMessage)).mkString("\n"))
          }).toValidationNel.flatMap { xso: Option[GunnySack] =>
            xso match {
              case Some(xs) => {
                (Validation.fromTryCatchThrowable[io.megam.auth.stack.AccountResult, Throwable] {
                  parse(xs.value).extract[AccountResult]
                } leftMap { t: Throwable =>
                  new ResourceItemNotFound(email, t.getMessage)
                }).toValidationNel.flatMap { j: AccountResult =>
                  Validation.success[Throwable, Option[AccountResult]](j.some).toValidationNel
                }
              }
              case None => Validation.failure[Throwable, Option[AccountResult]](new ResourceItemNotFound(email, "")).toValidationNel
            }
          }
        }
    }).get(email).eval(InMemoryCache[ValidationNel[Throwable, Option[AccountResult]]]())
  }

  implicit val sedimentAccountEmail = new Sedimenter[ValidationNel[Throwable, Option[AccountResult]]] {
    def sediment(maybeASediment: ValidationNel[Throwable, Option[AccountResult]]): Boolean = {
      val notSed = maybeASediment.isSuccess
      notSed
    }
  }

}
