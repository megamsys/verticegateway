package models.admin.reports

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scala.collection.immutable.ListMap

import models.tosca.KeyValueList
import net.liftweb.json._
import io.megam.util.Time
import org.joda.time.{DateTime, Period}
import org.joda.time.format.DateTimeFormat
import models.Constants.{JSON_CLAZ, REPORTSCLAZ, REPORT_BACKUPS}
import models.admin.{ReportInput, ReportResult}

//Reporter needs to extends trait Reporter or else you'll only get a NoOpReport
//Every report will have 3 steps (build, aggregate, and generate report data)
class Backups(ri: ReportInput) extends Reporter {

  def report: ValidationNel[Throwable, Option[ReportResult]] = {
    for {
      abt <-   build(ri.start_date, ri.end_date)  leftMap { err: NonEmptyList[Throwable] ⇒ err }
      sal <-   aggregate(abt).successNel
    } yield {
      ReportResult(REPORT_BACKUPS, sal.map(_.map(_.toKeyList)), REPORTSCLAZ, Time.now.toString).some
    }
  }


 def build(startdate: String, enddate: String): ValidationNel[Throwable, Seq[models.disks.BackupsResult]] = {
    for {
     a <- (models.disks.Backups.findByDateRange(startdate, enddate) leftMap { err: NonEmptyList[Throwable] ⇒ err })
   } yield {
       a
      }
  }


  def reportFor(email: String, org: String): ValidationNel[Throwable, Option[ReportResult]] = {
    for {
      abt <-   buildFor(email, org, ri.start_date, ri.end_date)  leftMap { err: NonEmptyList[Throwable] ⇒ err }
      sal <-   aggregate(abt).successNel
    } yield {
      ReportResult(REPORT_BACKUPS, sal.map(_.map(_.toKeyList)), REPORTSCLAZ, Time.now.toString).some
    }
  }

  def buildFor(email: String, org: String, startdate: String, enddate: String): ValidationNel[Throwable, Seq[models.disks.BackupsResult]] = {
     for {
      a <- (models.disks.Backups.findByDateRange(startdate, enddate) leftMap { err: NonEmptyList[Throwable] ⇒ err })
    } yield {
        a
      }
   }

  private def aggregate(bacs: Seq[models.disks.BackupsResult]) = {
   for {
      la <-  BackupsAggregate(bacs).some
    } yield la.aggregate
  }

}

case class BackupsAggregate(bacs: Seq[models.disks.BackupsResult]) {
  lazy val aggregate: Seq[BackupsReportResult] = bacs.map(bac =>  {
    BackupsReportResult(bac.id, bac.asm_id, bac.account_id, bac.name, bac.status, bac.image_id, bac.tosca_type,
      KeyValueList.toMap(bac.inputs), KeyValueList.toMap(bac.outputs), bac.created_at)
   })
}


case class BackupsReportResult(id: String, asm_id: String, account_id: String, name: String, status: String,
                        image_id: String, tosca_type: String, inputProps: Map[String, String],
                        outputProps: Map[String, String], created_at: DateTime) {
    val X = "x"
    val Y = "y"

    val ID   = "id"
    val ASM_ID = "asm_id"
    val ACCOUNT_ID = "account_id"
    val NAME = "name"
    val STATUS = "status"
    val IMAGE_ID = "image_id"
    val TOSCA_TYPE = "type"
    val NUMBER_OF_HOURS = "number_of_hours"
    val INPUTPROPS = "inputprops"
    val OUTPUTPROPS = "outputprops"
    val CREATED_AT = "created_at"



  def isEmpty(x: String) = Option(x).forall(_.isEmpty)

  def shouldZero = isEmpty(created_at.toString)

  def calculateHours =   if (shouldZero) {  "0" }
                         else  {
                           val runningTime =  (new Period(DateTime.parse(created_at.toString), new DateTime())).toStandardDuration.getStandardMinutes
                           (runningTime.toFloat/60).toString
                       }



  def toKeyList: models.tosca.KeyValueList = models.tosca.KeyValueList(
    ListMap((X -> created_at.toString),
        (Y -> "1"),
        (ID -> id),
        (ASM_ID -> asm_id),
        (ACCOUNT_ID -> account_id),
        (NAME -> name),
        (STATUS -> status),
        (IMAGE_ID -> image_id),
        (TOSCA_TYPE -> tosca_type),
        (CREATED_AT -> created_at.toString),
        (NUMBER_OF_HOURS -> calculateHours)))
}
