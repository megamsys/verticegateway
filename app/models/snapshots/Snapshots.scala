package models.snapshots

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import cache._
import db._
import models.base.RequestInput
import models.tosca.{ KeyValueField, KeyValueList}
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._

import utils.{DateHelper, StringStuff}
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
case class SnapshotsInput( asm_id: String, org_id: String, account_id: String, name: String, status: String, tosca_type: String)

case class SnapshotsResult(
  snap_id: String,
  asm_id:  String,
  org_id: String,
  account_id: String,
  name:   String,
  status: String,
  image_id: String,
  tosca_type: String,
  inputs: models.tosca.KeyValueList,
  outputs: models.tosca.KeyValueList,
  json_claz: String,
  created_at: DateTime)

object SnapshotsResult {
  def apply(snap_id: String, asm_id: String, org_id: String, account_id: String, name: String, status: String, image_id: String, tosca_type: String, inputs: models.tosca.KeyValueList, outputs: models.tosca.KeyValueList) = new SnapshotsResult(snap_id, asm_id, org_id, account_id, name, status, image_id, tosca_type, inputs, outputs, "Megam::Snapshots", DateTime.now())
}

sealed class SnapshotsSacks extends CassandraTable[SnapshotsSacks, SnapshotsResult] with ImplicitJsonFormats {

  object snap_id extends StringColumn(this) with  PartitionKey[String]
  object asm_id extends StringColumn(this) with PrimaryKey[String]
  object org_id extends StringColumn(this)
  object account_id extends StringColumn(this) with PrimaryKey[String]
  object name extends StringColumn(this)
  object status extends StringColumn(this)
  object image_id extends StringColumn(this)
  object tosca_type extends StringColumn(this)
  object inputs extends JsonListColumn[SnapshotsSacks, SnapshotsResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object outputs extends JsonListColumn[SnapshotsSacks, SnapshotsResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }
  object created_at extends DateTimeColumn(this)
  object json_claz extends StringColumn(this)

  def fromRow(row: Row): SnapshotsResult = {
    SnapshotsResult(
      snap_id(row),
      asm_id(row),
      org_id(row),
      account_id(row),
      name(row),
      status(row),
      image_id(row),
      tosca_type(row),
      inputs(row),
      outputs(row),
      json_claz(row),
      created_at(row))
  }
}

abstract class ConcreteSnapshots extends SnapshotsSacks with RootConnector {

  override lazy val tableName = "snapshots"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(sps: SnapshotsResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.snap_id, sps.snap_id)
      .value(_.asm_id, sps.asm_id)
      .value(_.org_id, sps.org_id)
      .value(_.account_id, sps.account_id)
      .value(_.name, sps.name)
      .value(_.status, sps.status)
      .value(_.image_id, sps.image_id)
      .value(_.tosca_type, sps.tosca_type)
      .value(_.inputs, sps.inputs)
      .value(_.outputs, sps.outputs)
      .value(_.json_claz, sps.json_claz)
      .value(_.created_at, sps.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def updateRecord(email: String, rip: SnapshotsResult, aor: Option[SnapshotsResult]): ValidationNel[Throwable, ResultSet] = {
    val oldstatus  = aor.get.status
    val newstatus  = rip.status

    val oldimage_id= aor.get.image_id
    val newimage_id = rip.image_id

    val res = update.where(_.account_id eqs email)
      .modify(_.status setTo StringStuff.NilOrNot(newstatus, oldstatus))
      .and(_.image_id setTo StringStuff.NilOrNot(newimage_id, oldimage_id))
      .future()
      Await.result(res, 5.seconds).successNel
  }

  def listRecords(email: String): ValidationNel[Throwable, Seq[SnapshotsResult]] = {
    val res = select.allowFiltering().where(_.account_id eqs email).fetch()
    Await.result(res, 5.seconds).successNel
  }

  def getRecord(snap_id: String, assembly_id: String,  email: String): ValidationNel[Throwable, Option[SnapshotsResult]] = {
    val res = select.allowFiltering().where(_.snap_id eqs snap_id).and(_.account_id eqs email).and(_.asm_id eqs assembly_id).one()
    Await.result(res, 5.seconds).successNel
  }


  def getRecords(assembly_id: String, email: String): ValidationNel[Throwable, Seq[SnapshotsResult]] = {
  val res = select.allowFiltering().where(_.account_id eqs email).and(_.asm_id eqs assembly_id).fetch()
    Await.result(res, 5.seconds).successNel
  }
 def deleteRecord(acc_id: String, asm_id: String, id: String): ValidationNel[Throwable, ResultSet] = {
val res = delete.where(_.account_id eqs acc_id).and(_.snap_id eqs id).and(_.asm_id eqs asm_id).future()
Await.result(res,5.seconds).successNel
}
}

object Snapshots extends ConcreteSnapshots {

private def mkSnapshotsSack(email: String, input: String): ValidationNel[Throwable, SnapshotsResult] = {
  val snapshotsInput: ValidationNel[Throwable, SnapshotsInput] = (Validation.fromTryCatchThrowable[SnapshotsInput, Throwable] {
    parse(input).extract[SnapshotsInput]
  } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel

  for {
    snap <- snapshotsInput
    uir <- (UID("sps").get leftMap { ut: NonEmptyList[Throwable] => ut })
  } yield {
    val uname =  uir.get._2.toString.substring(0, 5)
    val bvalue = Set(email)
    val json = new SnapshotsResult(uir.get._1 + uir.get._2, snap.asm_id, snap.org_id, email, snap.name + uname, snap.status, "", snap.tosca_type, List(), List(), "Megam::Snapshots", DateHelper.now())
    json
  }
}


def create(email: String, input: String): ValidationNel[Throwable, Option[SnapshotsResult]] = {
  for {
    wa <- (mkSnapshotsSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
    set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
  } yield {
    play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Snapshots.created success", Console.RESET))
    atPub(email, wa)
    wa.some
  }
}

def delete(email: String, asm_id: String, id: String): ValidationNel[Throwable, SnapshotsResult] = {
  for {
    wa <- (findBySnapId(id,asm_id, email) leftMap { t: NonEmptyList[Throwable] => t })
    set <- (deleteRecord(email, asm_id, id) leftMap { t: NonEmptyList[Throwable] => t })
  } yield {
    play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Snapshots.delete success", Console.RESET))
    wa
}
}
def update(email: String, input: String): ValidationNel[Throwable, SnapshotsResult] = {
  val ripNel: ValidationNel[Throwable, SnapshotsResult] = (Validation.fromTryCatchThrowable[SnapshotsResult,Throwable] {
    parse(input).extract[SnapshotsResult]
  } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
  for {
    rip <- ripNel
    qor <- (Snapshots.findBySnapId(rip.snap_id, rip.asm_id, email) leftMap { t: NonEmptyList[Throwable] => t })
    set <- updateRecord(email, rip, qor.some)
  } yield {
    qor
  }
}

  def findByEmail(accountID: String): ValidationNel[Throwable, Seq[SnapshotsResult]] = {
    (listRecords(accountID) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Snapshots = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[SnapshotsResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[SnapshotsResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[SnapshotsResult]](new ResourceItemNotFound(accountID, "Snapshots = nothing found.")).toValidationNel
    }

  }

  def findBySnapId(snap_id: String, assembly_id: String, email: String): ValidationNel[Throwable, SnapshotsResult] = {
    (getRecord(snap_id, assembly_id, email) leftMap { t: NonEmptyList[Throwable] ⇒
      new ServiceUnavailableError(snap_id, (t.list.map(m ⇒ m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { xso: Option[SnapshotsResult] ⇒
      xso match {
        case Some(xs) ⇒ {
          Validation.success[Throwable, SnapshotsResult](xs).toValidationNel
        }
        case None ⇒ Validation.failure[Throwable, SnapshotsResult](new ResourceItemNotFound(snap_id, "")).toValidationNel
      }
    }
  }

  def findById(assemblyID: String, email: String): ValidationNel[Throwable, Seq[SnapshotsResult]] = {
    (getRecords(assemblyID, email) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(assemblyID, "Snapshots = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[SnapshotsResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[SnapshotsResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[SnapshotsResult]](new ResourceItemNotFound(assemblyID, "Snapshots = nothing found.")).toValidationNel
    }

  }

  //We support attaching disks for a VM. When we do containers we need to rethink.
  private def atPub(email: String, wa: SnapshotsResult): ValidationNel[Throwable, SnapshotsResult] = {
    models.base.Requests.createAndPub(email, RequestInput(wa.snap_id, email, CATTYPE_TORPEDO, "", SNAPSHOT_CREATE, SNAPSHOT).json)
    wa.successNel[Throwable]
  }

  //We support attaching disks for a VM. When we do containers we need to rethink.
  private def dePub(email: String, wa: SnapshotsResult): ValidationNel[Throwable, SnapshotsResult] = {
    models.base.Requests.createAndPub(email, RequestInput(wa.snap_id, email, CATTYPE_TORPEDO, "", SNAPSHOT_REMOVE, SNAPSHOT).json)
    wa.successNel[Throwable]
  }

}
