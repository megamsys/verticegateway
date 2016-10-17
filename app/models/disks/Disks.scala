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
import models.tosca._
import models.json.tosca._
import models.json.tosca.carton._
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._
import app.MConfig
import models.base._
import wash._

import io.megam.util.Time
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

/**
 * @author ranjitha
 *
 */
case class DisksInput( asm_id: String, org_id: String, account_id: String, size: String,status: String) {
}
case class DisksResult(
  id: String,
  asm_id:  String,
  org_id: String,
  account_id: String,
  disk_id: String,
  size:  String,
  status:  String,
  json_claz: String,
  created_at: String) {
}

object DisksResult {
  def apply(id: String, asm_id: String, org_id: String, account_id: String, disk_id: String, size: String, status: String) = new DisksResult(id, asm_id, org_id, account_id, disk_id, size, status, "Megam::Disks", Time.now.toString)
}

sealed class DisksSacks extends CassandraTable[DisksSacks, DisksResult] {

  implicit val formats = DefaultFormats

  object id extends StringColumn(this) with  PartitionKey[String]
  object asm_id extends StringColumn(this) with PrimaryKey[String]
  object org_id extends StringColumn(this)
  object account_id extends StringColumn(this) with PrimaryKey[String]
  object disk_id extends StringColumn(this)
  object size extends StringColumn(this)
  object status extends StringColumn(this)
  object created_at extends StringColumn(this)
  object json_claz extends StringColumn(this)

  def fromRow(row: Row): DisksResult = {
    DisksResult(
      id(row),
      asm_id(row),
      org_id(row),
      account_id(row),
      disk_id(row),
      size(row),
      status(row),
      json_claz(row),
      created_at(row))
  }
}

abstract class ConcreteDisks extends DisksSacks with RootConnector {
  // you can even rename the table in the schema to whatever you like.
  override lazy val tableName = "disks"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(dk: DisksResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, dk.id)
      .value(_.asm_id, dk.asm_id)
      .value(_.org_id, dk.org_id)
      .value(_.account_id, dk.account_id)
      .value(_.disk_id, dk.disk_id)
      .value(_.size, dk.size)
      .value(_.status, dk.status)
      .value(_.json_claz, dk.json_claz)
      .value(_.created_at, dk.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(email: String): ValidationNel[Throwable, Seq[DisksResult]] = {
    val res = select.allowFiltering().where(_.account_id eqs email).fetch()
    Await.result(res, 5.seconds).successNel
  }
  def getRecords(assembly_id: String, email: String): ValidationNel[Throwable, Seq[DisksResult]] = {
    val res = select.allowFiltering().where(_.account_id eqs email).and(_.asm_id eqs assembly_id).fetch()
    Await.result(res, 5.seconds).successNel
  }

}

object Disks extends ConcreteDisks {

/**
 * A private method which chains computation to make GunnySack when provided with an input json, email.
 * parses the json, and converts it to eventsinput, if there is an error during parsing, a MalformedBodyError is sent back.
 * After that flatMap on its success and the account id information is looked up.
 * If the account id is looked up successfully, then yield the GunnySack object.
 */
private def mkDisksSack(email: String, input: String): ValidationNel[Throwable, DisksResult] = {
  val DiskInput: ValidationNel[Throwable, DisksInput] = (Validation.fromTryCatchThrowable[DisksInput, Throwable] {
    parse(input).extract[DisksInput]
  } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

  for {
    dsk <- DiskInput
    uir <- (UID("dsk").get leftMap { ut: NonEmptyList[Throwable] => ut })
  } yield {

    val bvalue = Set(email)
    val json = new DisksResult(uir.get._1 + uir.get._2, dsk.asm_id, dsk.org_id, email, "", dsk.size, dsk.status,  "Megam::Disks", Time.now.toString)
    json
  }
}

/*
 * create new Disk for the user.
 *
 */
def create(email: String, input: String): ValidationNel[Throwable, Option[DisksResult]] = {
  for {
    wa <- (mkDisksSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
    set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
  } yield {
    play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Disks.created success", Console.RESET))
    atPub(email, wa)
    wa.some
  }
}

  /*
   * An IO wrapped finder using an email. Upon fetching the account_id for an email,
   * the csarnames are listed on the index (account.id) in bucket `CSARs`.
   * Using a "csarname" as key, return a list of ValidationNel[List[CSARResult]]
   * Takes an email, and returns a Future[ValidationNel, List[Option[CSARResult]]]
   */
  def findByEmail(accountID: String): ValidationNel[Throwable, Seq[DisksResult]] = {
    (listRecords(accountID) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(accountID, "Disks = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[DisksResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[DisksResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[DisksResult]](new ResourceItemNotFound(accountID, "Disks = nothing found.")).toValidationNel
    }

  }

  def findById(assemblyID: String, email: String): ValidationNel[Throwable, Seq[DisksResult]] = {
    (getRecords(assemblyID, email) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(assemblyID, "Disk = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[DisksResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[DisksResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[DisksResult]](new ResourceItemNotFound(assemblyID, "Disks = nothing found.")).toValidationNel
    }

  }

  //We support attaching disks for a VM. When we do containers we need to rethink.
  private def atPub(email: String, wa: DisksResult): ValidationNel[Throwable, DisksResult] = {
    models.base.Requests.createAndPub(email, RequestInput(wa.id, CATTYPE_TORPEDO, "", ATTACH_DISK, DISKS).json)
    wa.successNel[Throwable]
  }

  //We support dettaching disks for a VM. When we do containers we need to rethink.
  private def dePub(email: String, wa: DisksResult): ValidationNel[Throwable, DisksResult] = {
    models.base.Requests.createAndPub(email, RequestInput(wa.id, CATTYPE_TORPEDO, "", DETACH_DISK, DISKS).json)
    wa.successNel[Throwable]
  }

}
