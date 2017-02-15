package models.billing

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps
import scala.collection.mutable.ListBuffer

import cache._
import db._
import models.Constants._
import io.megam.auth.stack.AccountResult
import io.megam.auth.stack.{ Name, Phone, Password, States, Approval, Dates, Suspend }
import models.tosca.{ KeyValueField, KeyValueList}
import io.megam.auth.funnel.FunnelErrors._

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._

import utils.DateHelper
import io.megam.util.Time
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.{DateTimeFormat,ISODateTimeFormat}

import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import controllers.stack.ImplicitJsonFormats


/**
 * @author ranjitha
 *
 */
case class ExternalobjectsInput(object_id: String, inputs: models.tosca.KeyValueList)

case class ExternalobjectsResult(
    id: String,
    account_id: String,
    object_id: String,
    inputs: models.tosca.KeyValueList,
    json_claz: String,
    created_at: DateTime)



sealed class ExternalobjectsSacks extends CassandraTable[ExternalobjectsSacks, ExternalobjectsResult] with ImplicitJsonFormats {

  object id extends StringColumn(this)
  object account_id extends StringColumn(this) with PartitionKey[String]
  object object_id extends StringColumn(this) with PrimaryKey[String]

  object inputs extends JsonListColumn[ExternalobjectsSacks, ExternalobjectsResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object json_claz extends StringColumn(this)
  object created_at extends DateTimeColumn(this)


  def fromRow(row: Row): ExternalobjectsResult = {
    ExternalobjectsResult(
      id(row),
      account_id(row),
      object_id(row),
      inputs(row),
      json_claz(row),
      created_at(row))
  }
}

abstract class ConcreteExternalobjects extends ExternalobjectsSacks with RootConnector {

  override lazy val tableName = "external_objects"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(qs: ExternalobjectsResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, qs.id)
      .value(_.account_id, qs.account_id)
      .value(_.object_id, qs.object_id)
      .value(_.inputs, qs.inputs)
      .value(_.json_claz, qs.json_claz)
      .value(_.created_at, qs.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def getRecords(email: String, id: String): ValidationNel[Throwable, Seq[ExternalobjectsResult]] = {
    val res = select.allowFiltering().where(_.account_id eqs email).and(_.object_id eqs id).fetch()
    Await.result(res, 5.seconds).successNel
  }
}

object Externalobjects extends ConcreteExternalobjects {


  private def mkExternalobjectsSack(email: String, input: String): ValidationNel[Throwable, ExternalobjectsResult] = {
    val externalobjectInput: ValidationNel[Throwable, ExternalobjectsInput] = (Validation.fromTryCatchThrowable[ExternalobjectsInput, Throwable] {
      parse(input).extract[ExternalobjectsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      extobj <- externalobjectInput
      uir <- (UID("EXO").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      new ExternalobjectsResult(uir.get._1 + uir.get._2, email, extobj.object_id, extobj.inputs, "Megam::Externalobjects", DateHelper.now())
    }
  }


  def create(email: String, input: String): ValidationNel[Throwable, Option[ExternalobjectsResult]] = {
    for {
      wa <- (mkExternalobjectsSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.GREEN, Console.BOLD, "ExternalObjects","|+| ✔", Console.RESET))
      wa.some
    }
  }

  def findById(email: String, id: String): ValidationNel[Throwable, Seq[ExternalobjectsResult]] = {
    (getRecords(email, id) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(id, "Externalobjects = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[ExternalobjectsResult] =>
     if (!nm.isEmpty)
        Validation.success[Throwable, Seq[ExternalobjectsResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[ExternalobjectsResult]](new ResourceItemNotFound(id, "Externalobjects = nothing found.")).toValidationNel
    }
  }
}
