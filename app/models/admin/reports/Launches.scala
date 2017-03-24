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
import models.Constants.{JSON_CLAZ, REPORTSCLAZ, REPORT_LAUNCHES, REPORT_CATEGORYMAP}
import models.admin.{ReportInput, ReportResult}

class Launches(ri: ReportInput) extends Reporter {

  def report: ValidationNel[Throwable, Option[ReportResult]] = {
    for {
      abt <-   build(ri.start_date, ri.end_date)  leftMap { err: NonEmptyList[Throwable] ⇒ err }
      aal <-   aggregate(abt).successNel
      fal <-   subaggregate(aal).successNel
    } yield {
        ReportResult(REPORT_LAUNCHES, fal.map(_.map(_.toKeyList)), REPORTSCLAZ, Time.now.toString).some
    }
  }

 def build(startdate: String, enddate: String): ValidationNel[Throwable,Tuple2[Seq[models.tosca.AssembliesResult],
                Seq[models.tosca.AssemblyResult]]] = {
    for {
     al <- (models.tosca.Assemblies.findByDateRange(startdate, enddate) leftMap { err: NonEmptyList[Throwable] ⇒ err })
     as <- (models.tosca.Assembly.findByDateRange(startdate, enddate) leftMap { err: NonEmptyList[Throwable] ⇒ err })
   } yield {
      (al, as )
    }
  }

  def reportFor(email: String, org: String): ValidationNel[Throwable, Option[ReportResult]] = {
    for {
      abt <-   buildFor(email, org,  ri.start_date, ri.end_date)  leftMap { err: NonEmptyList[Throwable] ⇒ err }
      aal <-   aggregate(abt).successNel
    } yield {
      ReportResult(REPORT_LAUNCHES, aal.map(_.map(_.toKeyList)), REPORTSCLAZ, Time.now.toString).some
    }
  }

 def buildFor(email: String, org: String, startdate: String, enddate: String): ValidationNel[Throwable,Tuple2[Seq[models.tosca.AssembliesResult],
                Seq[models.tosca.AssemblyResult]]] = {
    for {
     al <- (models.tosca.Assemblies.findByDateRangeFor(email, org,  startdate, enddate) leftMap { err: NonEmptyList[Throwable] ⇒ err })
     as <- (models.tosca.Assembly.findByDateRangeFor(email, org, startdate, enddate) leftMap { err: NonEmptyList[Throwable] ⇒ err })
   } yield (al, as)
  }


  private def aggregate(abt: Tuple2[Seq[models.tosca.AssembliesResult], Seq[models.tosca.AssemblyResult]]) = {
   for {
     ba <- (abt._1.map { asms => (asms.assemblies.map {x => (x, asms.id)})}).flatten.toMap.some
     la <-  LaunchesAggregate(abt._2, ba).some
   } yield {
     la.aggregate
   }
  }

  private def subaggregate(olrt: Option[Seq[LaunchesResult]])  = {
  val lrt = olrt.getOrElse(List[LaunchesResult]())

  val f: List[String] = REPORT_CATEGORYMAP.get(ri.category).getOrElse(REPORT_CATEGORYMAP.get("all").getOrElse(List()))
  val g: List[String] = List(ri.group)

  for {
      ba <- lrt.filter { a => {

         (f.filter(x => a.tosca_type.contains(x)).size > 0) &&
         (if (g.size > 2) (g.filter(x => a.status.contains(x)).size > 0) else  true)
      }
    }.some
  } yield  {
    ba
  }
  }
}


case class LaunchesAggregate(als: Seq[models.tosca.AssemblyResult],
                             tal: Map[String, String]) {

  lazy val aggregate: Seq[LaunchesResult] = als.map(al =>  {
    LaunchesResult(al.id, tal.get(al.id).getOrElse(""), al.name, al.account_id, al.state, al.status, al.tosca_type,
      al.inputs, al.outputs, al.created_at)
   })
}

case class LaunchesResult(id: String, asms_id: String, name: String, account_id: String, state: String, status: String, tosca_type: String,
                         inputs: KeyValueList, outputs: KeyValueList, created_at: DateTime) {
  val X = "x"
  val Y = "y"

  val ID   = "id"
  val ASMS_ID = "asms_id"
  val NAME = "name"
  val ACCOUNT_ID = "account_id"
  val STATE = "state"
  val STATUS = "status"
  val TOSCA_TYPE = "type"
  val CREATED_AT = "created_at"
  val NUMBER_OF_HOURS = "number_of_hours"

  def isEmpty(x: String) = Option(x).forall(_.isEmpty)

  lazy val shouldZero = isEmpty(created_at.toString)

  lazy val calculateHours =  if (shouldZero) {  "0" } else  {
                             val  hoursObject = org.joda.time.Hours.hoursBetween(
                             DateTime.parse(created_at.toString), new DateTime())
                             hoursObject.getHours.toString
                            }

  lazy val  shortenedCreatedAt =  {
    val dt =  DateTime.parse(created_at.toString)
    dt.monthOfYear.getAsText + "-" + dt.dayOfMonth.getAsText
  }

  lazy val labelledName = models.tosca.KeyValueList.find(inputs,models.tosca.PatternConstants.LAB_NAME).map(_.mkString)
  def toKeyList: models.tosca.KeyValueList = models.tosca.KeyValueList(
    ListMap((X -> created_at.toString),
        (Y -> "1"),
        (ID -> id),
        (ASMS_ID -> asms_id),
        (NAME -> labelledName.getOrElse(name)),
        (ACCOUNT_ID -> account_id),
        (STATE -> status),
        (STATUS -> state),
        (TOSCA_TYPE -> tosca_type),
        (CREATED_AT -> shortenedCreatedAt),
        (NUMBER_OF_HOURS -> calculateHours)))

}
