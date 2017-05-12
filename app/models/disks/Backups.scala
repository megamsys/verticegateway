package models.disks

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
case class BackupsInput( asm_id: String, org_id: String, account_id: String, name: String, status: String, tosca_type: String,
                         inputs: models.tosca.KeyValueList, outputs: models.tosca.KeyValueList)

case class BackupsResult(
  id: String,
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

object BackupsResult {
  def apply(id: String, asm_id: String, org_id: String, account_id: String, name: String, status: String, image_id: String, tosca_type: String, inputs: models.tosca.KeyValueList, outputs: models.tosca.KeyValueList) = new BackupsResult(id, asm_id, org_id, account_id, name, status, image_id, tosca_type, inputs, outputs, "Megam::Backups", DateTime.now())
}

sealed class BackupsSacks extends CassandraTable[BackupsSacks, BackupsResult] with ImplicitJsonFormats {

  object id extends StringColumn(this) with  PartitionKey[String]
  object asm_id extends StringColumn(this) with PrimaryKey[String]
  object org_id extends StringColumn(this)
  object account_id extends StringColumn(this) with PrimaryKey[String]
  object name extends StringColumn(this)
  object status extends StringColumn(this)
  object image_id extends StringColumn(this)
  object tosca_type extends StringColumn(this)
  object inputs extends JsonListColumn[BackupsSacks, BackupsResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object outputs extends JsonListColumn[BackupsSacks, BackupsResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }
  object created_at extends DateTimeColumn(this)
  object json_claz extends StringColumn(this)

  def fromRow(row: Row): BackupsResult = {
    BackupsResult(
      id(row),
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

abstract class ConcreteBackups extends BackupsSacks with RootConnector {

  override lazy val tableName = "backups"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(sps: BackupsResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, sps.id)
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

  def updateRecord(email: String, rip: BackupsResult, aor: Option[BackupsResult]): ValidationNel[Throwable, ResultSet] = {
    val oldstatus  = aor.get.status
    val newstatus  = rip.status

    val oldimage_id= aor.get.image_id
    val newimage_id = rip.image_id

    val res = update.where(_.id eqs rip.id)
        .and(_.account_id eqs email)
        .and(_.asm_id eqs rip.asm_id)
      .modify(_.status setTo StringStuff.NilOrNot(newstatus, oldstatus))
      .and(_.image_id setTo StringStuff.NilOrNot(newimage_id, oldimage_id))
      .and(_.inputs setTo rip.inputs)
      .and(_.outputs setTo rip.outputs)
      .future()
      Await.result(res, 5.seconds).successNel
  }

  def listRecords(email: String): ValidationNel[Throwable, Seq[BackupsResult]] = {
    val res = select.allowFiltering().where(_.account_id eqs email).fetch()
    Await.result(res, 5.seconds).successNel
  }

  def listAllRecords: ValidationNel[Throwable, Seq[BackupsResult]] = {
    val res = select.fetch
    Await.result(res, 5.seconds).successNel
  }


  def getRecord(id: String, assembly_id: String,  email: String): ValidationNel[Throwable, Option[BackupsResult]] = {
    val res = select.allowFiltering().where(_.id eqs id).and(_.account_id eqs email).and(_.asm_id eqs assembly_id).one()
    Await.result(res, 5.seconds).successNel
  }


  def getRecords(assembly_id: String, email: String): ValidationNel[Throwable, Seq[BackupsResult]] = {
  val res = select.allowFiltering().where(_.account_id eqs email).and(_.asm_id eqs assembly_id).fetch()
    Await.result(res, 5.seconds).successNel
  }
  def getBackupRecords(id: String, email: String): ValidationNel[Throwable, Seq[BackupsResult]] = {
  val res = select.allowFiltering().where(_.id eqs id).and(_.account_id eqs email).fetch()
    Await.result(res, 5.seconds).successNel
  }

 def deleteRecord(acc_id: String, asm_id: String, id: String): ValidationNel[Throwable, ResultSet] = {
   val res = delete.where(_.account_id eqs acc_id).and(_.id eqs id).and(_.asm_id eqs asm_id).future()
   Await.result(res,5.seconds).successNel
 }
}

object Backups extends ConcreteBackups {

private def mkBackupsSack(email: String, input: String): ValidationNel[Throwable, BackupsResult] = {
  val backupsInput: ValidationNel[Throwable, BackupsInput] = (Validation.fromTryCatchThrowable[BackupsInput, Throwable] {
    parse(input).extract[BackupsInput]
  } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel

  for {
    back <- backupsInput
    uir <- (UID("BAK").get leftMap { ut: NonEmptyList[Throwable] => ut })
  } yield {
    val uname =  uir.get._2.toString.substring(0, 5)
    val json = new BackupsResult(uir.get._1 + uir.get._2, back.asm_id, back.org_id, email, back.name + uname, back.status, "", back.tosca_type, back.inputs, back.outputs, "Megam::Backups", DateHelper.now())
    json
  }
}


def create(email: String, input: String): ValidationNel[Throwable, Option[BackupsResult]] = {
  for {
    wa <- (mkBackupsSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
    set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
  } yield {
    play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.GREEN, Console.BOLD, "Backups","|+| ✔", Console.RESET))
    atPub(email, wa)
    wa.some
  }
}

def delete(email: String, asm_id: String, id: String): ValidationNel[Throwable, BackupsResult] = {
  for {
    wa <- (findByIdAndAssemblyId(id,asm_id, email) leftMap { t: NonEmptyList[Throwable] => t })
    set <- (deleteRecord(email, asm_id, id) leftMap { t: NonEmptyList[Throwable] => t })
  } yield {
    play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.RED, Console.BOLD, "Backups","|-| ✔", Console.RESET))
    wa
}
}
def update(email: String, input: String): ValidationNel[Throwable, BackupsResult] = {
  val ripNel: ValidationNel[Throwable, BackupsResult] = (Validation.fromTryCatchThrowable[BackupsResult,Throwable] {
    parse(input).extract[BackupsResult]
  } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
  for {
    rip <- ripNel
    qor <- (Backups.findByIdAndAssemblyId(rip.id, rip.asm_id, email) leftMap { t: NonEmptyList[Throwable] => t })
    set <- updateRecord(email, rip, qor.some)
  } yield {
    qor
  }
}

  def findByEmail(accountID: String): ValidationNel[Throwable, Seq[BackupsResult]] = {
    (listRecords(accountID) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Backups = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[BackupsResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[BackupsResult]](nm).toValidationNel
      else
        Validation.success[Throwable, Seq[BackupsResult]](List[BackupsResult]()).toValidationNel
    }
  }

  def findByDateRange(startdate: String, enddate: String): ValidationNel[Throwable, Seq[BackupsResult]] = {
    listAllRecords match {
      case Success(value) => Validation.success[Throwable, Seq[BackupsResult]](value).toValidationNel
      case Failure(err) => Validation.success[Throwable, Seq[BackupsResult]](List()).toValidationNel
    }
  }

  def deleteByEmail(email: String): ValidationNel[Throwable, BackupsResult] = {
    for {
      sa <- (findByEmail(email) leftMap { t: NonEmptyList[Throwable] => t })
      df <- deleteFound(email, sa)
    } yield df
  }

  def deleteByAssembly(assemblyID: String, email: String): ValidationNel[Throwable, BackupsResult] = {
    for {
      wa <- (findByAssemblyId(assemblyID, email) leftMap { t: NonEmptyList[Throwable] => t })
      df <- deleteFound(email, wa)
    } yield df
  }

  def findByIdAndAssemblyId(id: String, assembly_id: String, email: String): ValidationNel[Throwable, BackupsResult] = {
    (getRecord(id, assembly_id, email) leftMap { t: NonEmptyList[Throwable] ⇒
      new ServiceUnavailableError(id, (t.list.map(m ⇒ m.getMessage)).mkString("\n"))
    }).toValidationNel.flatMap { xso: Option[BackupsResult] ⇒
      xso match {
        case Some(xs) ⇒ {
          Validation.success[Throwable, BackupsResult](xs).toValidationNel
        }
        case None ⇒ Validation.failure[Throwable, BackupsResult](new ResourceItemNotFound(id, "")).toValidationNel
      }
    }
  }

  def findByAssemblyId(assemblyID: String, email: String): ValidationNel[Throwable, Seq[BackupsResult]] = {
    (getRecords(assemblyID, email) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(assemblyID, "Backups = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[BackupsResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[BackupsResult]](nm).toValidationNel
      else
      Validation.success[Throwable, Seq[BackupsResult]](List[BackupsResult]()).toValidationNel
    }

  }

  //Admin authority can list all snapshots for 1.5.
  def list: ValidationNel[Throwable, Seq[BackupsResult]] = {
    listAllRecords match {
      case Success(value) => Validation.success[Throwable, Seq[BackupsResult]](value).toValidationNel
      case Failure(err) => Validation.success[Throwable, Seq[BackupsResult]](List()).toValidationNel
    }

  }

  def findById(id: String, email: String): ValidationNel[Throwable, Seq[BackupsResult]] = {
    (getBackupRecords(id, email) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(id, "Backups = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[BackupsResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[BackupsResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[BackupsResult]](new ResourceItemNotFound(id, "Backups = nothing found.")).toValidationNel
    }
  }

  private def deleteFound(email: String, sn: Seq[BackupsResult]) = {
      val output = (sn.map { sas =>
        play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.RED, Console.BOLD, "Backups","|-| ✔", Console.RESET))
        dePub(email, sas)
      })

      if (!output.isEmpty)
         output.head
      else
        BackupsResult("","","","","","","", "", models.tosca.KeyValueList.empty, models.tosca.KeyValueList.empty).successNel
  }


  //We support attaching disks for a VM. When we do containers we need to rethink.
  private def atPub(email: String, wa: BackupsResult): ValidationNel[Throwable, BackupsResult] = {
    models.base.Requests.createAndPub(email, RequestInput(email, wa.id, CATTYPE_TORPEDO, "", BACKUP_CREATE, BACKUP).json)
    wa.successNel[Throwable]
  }

  //We support attaching disks for a VM. When we do containers we need to rethink.
  private def dePub(email: String, wa: BackupsResult): ValidationNel[Throwable, BackupsResult] = {
    models.base.Requests.createAndPub(email, RequestInput(email, wa.id, CATTYPE_TORPEDO, "", BACKUP_REMOVE, BACKUP).json)
    wa.successNel[Throwable]
  }

}
