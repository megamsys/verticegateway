package models.admin.reports

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import models.tosca.KeyValueList
import net.liftweb.json._
import io.megam.util.Time
import org.joda.time.{DateTime, Period}
import org.joda.time.format.DateTimeFormat
import models.Constants.{JSON_CLAZ, REPORTSCLAZ, REPORT_LAUNCHES}
import models.admin.{ReportInput, ReportResult}

class Launches(ri: ReportInput) extends Reporter {

  def report: ValidationNel[Throwable, Option[ReportResult]] = {
    for {
      abt <-   build(ri.start_date, ri.end_date)  leftMap { err: NonEmptyList[Throwable] ⇒ err }
      aal <-   LaunchesAggregate(abt).aggregate.successNel
    } yield {
      ReportResult(REPORT_LAUNCHES, aal.map(_.toKeyList).some, REPORTSCLAZ, Time.now.toString).some
    }
  }

 def build(startdate: String, enddate: String): ValidationNel[Throwable,Seq[models.tosca.AssemblyResult]] = {
    for {
     a <- (models.tosca.Assembly.findByDateRange(startdate, enddate) leftMap { err: NonEmptyList[Throwable] ⇒ err })
   } yield a
  }
}

case class LaunchesAggregate(als: Seq[models.tosca.AssemblyResult]) {
  lazy val aggregate: Seq[LaunchesResult] = als.map(al =>  {
    LaunchesResult(al.id, al.name, al.account_id, al.state, al.status, al.tosca_type,
      KeyValueList.toMap(al.inputs), KeyValueList.toMap(al.outputs), al.created_at)
   })
}

case class LaunchesResult(id: String, name: String, account_id: String, state: String, status: String, tosca_type: String,
                         inputProps: Map[String, String], outputProps: Map[String, String], created_at: DateTime) {
    val X = "x"
    val Y = "y"

    val ID   = "id"
    val NAME = "name"
    val ACCOUNT_ID = "account_id"
    val STATE = "state"
    val STATUS = "status"
    val TOSCA_TYPE = "type"
    val CREATED_AT = "created_at"
    val INPUTPROPS = "inputprops"
    val OUTPUTPROPS = "resultprops"

    val NUMBER_OF_HOURS = "number_of_hours"

  def isEmpty(x: String) = Option(x).forall(_.isEmpty)

  def shouldZero = isEmpty(created_at.toString)

  def calculateHours =   if (shouldZero) {  "0" }
                         else  {
                           val runningTime =  (new Period(created_at, DateTime.parse(Time.now.toString))).toStandardDuration.getStandardMinutes
                           (runningTime.toFloat/60).toString
                       }



  def toKeyList: models.tosca.KeyValueList = models.tosca.KeyValueList(
    Map((X -> created_at.toString),
        (Y -> "0"),
        (ID -> id),
        (NAME -> name),
        (ACCOUNT_ID -> account_id),
        (STATE -> status),
        (STATUS -> state),
        (TOSCA_TYPE -> tosca_type),
        (INPUTPROPS -> inputProps.map(pair => pair._1+"="+pair._2).mkString("",":",",")),
        (OUTPUTPROPS -> outputProps.map(pair => pair._1+"="+pair._2).mkString("",":",",")),
        (CREATED_AT -> created_at.toString),
        (NUMBER_OF_HOURS -> calculateHours)))
}
