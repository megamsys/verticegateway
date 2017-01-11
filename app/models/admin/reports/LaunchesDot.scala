package models.admin.reports

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import net.liftweb.json._
import io.megam.util.Time
import org.joda.time.{DateTime, Period}
import org.joda.time.format.DateTimeFormat
import models.Constants.{JSON_CLAZ, REPORTSCLAZ, REPORT_LANDOT}
import models.Constants.{REPORT_FILTER_VM, REPORT_FILTER_VERTICE_PREPACKAGED, REPORT_FILTER_BITNAMI_PREPACKAGED}
import models.Constants.{REPORT_DEAD, REPORT_NOTINITED}
import models.Constants.{REPORT_FILTER_CUSTOMAPPS, REPORT_FILTER_CONTAINERS}
import models.admin.{ReportInput, ReportResult}

class LaunchesDot(ri: ReportInput) extends Reporter {

  def report: ValidationNel[Throwable, Option[ReportResult]] = {
    for {
      alt <-   build(ri.start_date, ri.end_date)  leftMap { err: NonEmptyList[Throwable] ⇒ err }
      vdt <-   aggregate(alt, REPORT_FILTER_VM).successNel  leftMap { err: NonEmptyList[Throwable] ⇒ err }
      ddt <-   subaggregate(alt, REPORT_FILTER_VM, REPORT_DEAD).successNel  leftMap { err: NonEmptyList[Throwable] ⇒ err }
      fdt <-   subaggregate(alt, REPORT_FILTER_VM, REPORT_NOTINITED).successNel  leftMap { err: NonEmptyList[Throwable] ⇒ err }
      edt <-   aggregate(alt, REPORT_FILTER_VERTICE_PREPACKAGED).successNel  leftMap { err: NonEmptyList[Throwable] ⇒ err }
      bdt <-   aggregate(alt, REPORT_FILTER_BITNAMI_PREPACKAGED).successNel  leftMap { err: NonEmptyList[Throwable] ⇒ err }
      cut <-   aggregate(alt, REPORT_FILTER_CUSTOMAPPS).successNel leftMap { err: NonEmptyList[Throwable] ⇒ err }
      cdt <-   aggregate(alt, REPORT_FILTER_CONTAINERS).successNel leftMap { err: NonEmptyList[Throwable] ⇒ err }
      pdt <-   popular(alt).successNel leftMap { err: NonEmptyList[Throwable] ⇒ err }
    } yield {
      ReportResult(REPORT_LANDOT,
          new LaunchCounted(vdt, ddt, fdt, edt, bdt, cut, cdt, pdt).toKeyList.asInstanceOf[Seq[models.tosca.KeyValueList]].some,
         REPORTSCLAZ, Time.now.toString).some

    }
  }

  def build(startdate: String, enddate: String): ValidationNel[Throwable,Seq[models.tosca.AssemblyResult]] = {
     for {
      a <- (models.tosca.Assembly.findByDateRange(startdate, enddate) leftMap { err: NonEmptyList[Throwable] ⇒ err })
    } yield a
   }

   def aggregate(abt: Seq[models.tosca.AssemblyResult], f: List[String]) = {
    for {
      ba <- abt.filter { a => (f.filter(x => a.tosca_type.contains(x)).size > 0) }.some
    } yield  ba.size.toString
   }

   def subaggregate(abt: Seq[models.tosca.AssemblyResult], f: List[String], g: List[String]) = {
     for {
       ba <- abt.filter { a =>
         (f.filter(x => a.tosca_type.contains(x)).size > 0)  &&
         (g.filter(x => a.status.contains(x)).size > 0)
       }.some
     } yield  ba.size.toString
   }

   def popular(abt: Seq[models.tosca.AssemblyResult]) = {
    for {
      ba <- (abt.groupBy(_.tosca_type).map { case (k,v) => (k -> v.size.toInt) }).some
    } yield {
      if (!ba.isEmpty) ba.max else ("",0)
    }
   }


}


case class LaunchCounted(vm: Option[String],
                         dead_vms: Option[String],
                         notinited_vms: Option[String],
                         vertice_prepackaged: Option[String],
                         bitnami_prepackaged: Option[String],
                         customapps: Option[String],
                         containers: Option[String],
                         popular: Option[(String, Int)]) {
  private val X = "x"
  private val Y = "y"

  private val VMS           = "total_vms"
  private val VMS_DEAD      = "total_vms_dead"
  private val VMS_NOTINITED = "total_vms_notinited"

  private val VERTICE      = "total_prepackaged_vertice"
  private val BITNAMI      = "total_prepackaged_bitnami"
  private val CUSTOM       = "total_customapps"
  private val CONTAINERS   = "total_containers"
  private val POPULAR      = "most_popular"


  def toKeyList: models.tosca.KeyValueList = models.tosca.KeyValueList(
    Map((X -> "launchesdot" ),
        (Y -> "nos"),
        (VMS -> vm.getOrElse("0")),
        (VMS_DEAD -> dead_vms.getOrElse("0")),
        (VMS_NOTINITED -> notinited_vms.getOrElse("0")),
        (VERTICE -> vertice_prepackaged.getOrElse("0")),
        (BITNAMI -> bitnami_prepackaged.getOrElse("0")),
        (CUSTOM -> customapps.getOrElse("0")),
        (CONTAINERS -> containers.getOrElse("0")),
        (POPULAR -> popular.getOrElse(("none yet.","0")).toString)
      )
    )
}
