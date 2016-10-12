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

import io.megam.common.uid.UID
import io.megam.util.Time
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

import io.megam.auth.stack.MasterKeyResult

import java.util.UUID
import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future ⇒ ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.annotation.tailrec

/**
 * @author rajthilak
 */
case class MasterKeysInput(key: String) {
  val half_json = "\"key\":\"" + key + "\""

  val json = "{" + half_json + "}"
}

sealed class MasterKeysSacks extends CassandraTable[MasterKeysSacks, MasterKeyResult] {

  implicit val formats = DefaultFormats

  object id extends StringColumn(this) with PrimaryKey[String]
  object key extends StringColumn(this)
  object created_at extends StringColumn(this)

  def fromRow(row: Row): MasterKeyResult = {
    MasterKeyResult(
      id(row),
      key(row),
      created_at(row))
  }
}

abstract class ConcreteMasterKeys extends MasterKeysSacks with RootConnector {
  // you can even rename the table in the schema to whatever you like.
  override lazy val tableName = "master_keys"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(ams: MasterKeyResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, ams.id)
      .value(_.key, ams.key)
      .value(_.created_at, ams.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def getRecord(id: String): ValidationNel[Throwable, Option[MasterKeyResult]] = {
    val res = select.allowFiltering().where(_.id eqs id).one()
    Await.result(res, 5.seconds).successNel
  }

}

object MasterKeys extends ConcreteMasterKeys {

  private def mkMasterKeysSack(input: String): ValidationNel[Throwable, Option[MasterKeyResult]] = {
    val ripNel: ValidationNel[Throwable, MasterKeysInput] = (Validation.fromTryCatchThrowable[models.base.MasterKeysInput, Throwable] {
      parse(input).extract[MasterKeysInput]
    } leftMap { t: Throwable ⇒ new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
    for {
      rip ← ripNel
    } yield {
      val res = MasterKeyResult("1", rip.key, Time.now.toString)
      res.some
    }
  }

  def create(input: String): ValidationNel[Throwable, Option[MasterKeyResult]] = {
    for {
      wa ← (mkMasterKeysSack(input) leftMap { err: NonEmptyList[Throwable] ⇒ err })
      set ← (insertNewRecord(wa.get) leftMap { t: NonEmptyList[Throwable] ⇒ t })
    } yield {
      wa
    }
  }

  def findById(id: String): ValidationNel[Throwable, Option[MasterKeyResult]] = {
    InMemory[ValidationNel[Throwable, Option[MasterKeyResult]]]({
      name: String ⇒
        {
          play.api.Logger.debug(("%-20s -->[%s]").format("Master key id ->", id))
          (getRecord(id) leftMap { t: NonEmptyList[Throwable] ⇒
            new ServiceUnavailableError(id, (t.list.map(m ⇒ m.getMessage)).mkString("\n"))
          }).toValidationNel.flatMap { xso: Option[MasterKeyResult] ⇒
            xso match {
              case Some(xs) ⇒ {
                Validation.success[Throwable, Option[MasterKeyResult]](xs.some).toValidationNel
              }
              case None ⇒ Validation.failure[Throwable, Option[MasterKeyResult]](new ResourceItemNotFound(id, "")).toValidationNel
            }
          }
        }
    }).get(id).eval(InMemoryCache[ValidationNel[Throwable, Option[MasterKeyResult]]]())

  }

  implicit val sedimentMasterKey = new Sedimenter[ValidationNel[Throwable, Option[MasterKeyResult]]] {
    def sediment(maybeASediment: ValidationNel[Throwable, Option[MasterKeyResult]]): Boolean = {
      maybeASediment.isSuccess
    }
  }

}
