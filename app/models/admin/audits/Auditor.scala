package models.admin.audits

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import cache._
import db._
import models.tosca._
import models.json.tosca._
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._
import models.base._
import wash._

import utils.DateHelper
import io.megam.util.Time
import org.joda.time.{DateTime, DateTimeZone}
import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._
import com.websudos.phantom.iteratee.Iteratee
import controllers.stack.ImplicitJsonFormats

case class AuditLogInput(account_id: String, audit_type: String, data: KeyValueList)

case class AuditLogResult(id: String, account_id: String,  created_at: DateTime,
                       audit_type: String, data: models.tosca.KeyValueList, json_claz: String)

object AuditLogResult {
  def apply(id: String,
            account_id: String,
            created_at: DateTime,
            audit_type: String,
            data: models.tosca.KeyValueList) = new AuditLogResult(id, account_id, created_at, audit_type, data, ADMINAUDITLOGCLAZ)
}

sealed class AuditLogSacks extends CassandraTable[AuditLogSacks, AuditLogResult] with ImplicitJsonFormats {

  object account_id extends StringColumn(this) with PartitionKey[String]
  object created_at extends DateTimeColumn(this) with PrimaryKey[DateTime]
  object audit_type extends StringColumn(this) with PrimaryKey[String]

  object data extends JsonListColumn[AuditLogSacks, AuditLogResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object id extends StringColumn(this)
  object json_claz extends StringColumn(this)

  def fromRow(row: Row): AuditLogResult = {
    AuditLogResult(
      id(row),
      account_id(row),
      created_at(row),
      audit_type(row),
      data(row),
      json_claz(row))
  }
}

abstract class ConcreteAuditLog extends AuditLogSacks with RootConnector {
  override lazy val tableName = "audit_logs"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(evt: AuditLogResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, evt.id)
      .value(_.account_id, evt.account_id)
      .value(_.created_at, evt.created_at)
      .value(_.audit_type, evt.audit_type)
      .value(_.data, evt.data)
      .value(_.json_claz, evt.json_claz)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(email: String, limit: String): ValidationNel[Throwable, Seq[AuditLogResult]] = {
    val count = (if (limit == "0")  "10" else  limit)
    val res = select.where(_.account_id eqs email).limit(count.toInt).fetch()
    Await.result(res, 5.seconds).successNel
    }


  def indexRecords(email: String): ValidationNel[Throwable, Seq[AuditLogResult]] = {
    val res = select.where(_.account_id eqs email).fetch()
    Await.result(res, 5.seconds).successNel

  }

  def getRecords(created_at: DateTime, email: String, limit: String): ValidationNel[Throwable, Seq[AuditLogResult]] = {
    val count = (if (limit == "0")  "10" else  limit)
     val times = DateHelper.toTimeRange(created_at)
     val res = select.allowFiltering().where(_.created_at gte times._1).and(_.created_at lte times._2).and(_.account_id eqs email).limit(count.toInt).fetch()
    Await.result(res, 5.seconds).successNel
  }

  def deleteRecords(email: String): ValidationNel[Throwable, ResultSet] = {
    val res = delete.where(_.account_id eqs email).future()
    Await.result(res, 5.seconds).successNel
  }

}

object AuditLog extends ConcreteAuditLog {

private def mkAuditLogSack(email: String, input: String): ValidationNel[Throwable, AuditLogResult] = {
  val nelBill: ValidationNel[Throwable, AuditLogInput] = (Validation.fromTryCatchThrowable[AuditLogInput, Throwable] {
    parse(input).extract[AuditLogInput]
  } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure
  for {
    bil <- nelBill
    uir <- (UID("AUL").get leftMap { ut: NonEmptyList[Throwable] => ut })
  } yield {
     new AuditLogResult(uir.get._1 + uir.get._2,email,DateHelper.now(), bil.audit_type, bil.data, ADMINAUDITLOGCLAZ)
  }
}


def create(email: String, input: String): ValidationNel[Throwable, Option[AuditLogResult]] = {
  for {
    wa <- (mkAuditLogSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
    set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
  } yield {
    play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.GREEN, Console.BOLD, "AuditLog", "|+| ✔", Console.RESET))
    wa.some
  }
 }

  def createFrom(email: String, input: AuditLogInput) = create(email,compactRender(Extraction.decompose(input)))

  def findByEmail(accountID: String, limit: String): ValidationNel[Throwable, Seq[AuditLogResult]] = {
    (listRecords(accountID, limit) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Events = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[AuditLogResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[AuditLogResult]](nm).toValidationNel
       else
        Validation.failure[Throwable, Seq[AuditLogResult]](new ResourceItemNotFound(accountID, "AuditLog = nothing found.")).toValidationNel

    }
  }

  def findById(email: String, input: String, limit: String): ValidationNel[Throwable, Seq[AuditLogResult]] = {
   (mkAuditLogSack(email, input) leftMap { err: NonEmptyList[Throwable] => err
   }).flatMap { ws: AuditLogResult =>
    (getRecords(ws.created_at.withTimeAtStartOfDay(), email, limit) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(ws.account_id, "AuditLogs = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[AuditLogResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[AuditLogResult]](nm).toValidationNel
       else
        Validation.failure[Throwable, Seq[AuditLogResult]](new ResourceItemNotFound(ws.account_id, "AuditLog = nothing found.")).toValidationNel
    }
  }
}

  def delete(email: String): ValidationNel[Throwable, Option[AuditLogResult]] = {
    deleteRecords(email) match {
      case Success(value) => Validation.success[Throwable, Option[AuditLogResult]](none).toValidationNel
      case Failure(err) => Validation.success[Throwable, Option[AuditLogResult]](none).toValidationNel
    }
  }
}
