package models.admin.reports

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scala.collection.immutable.ListMap

import net.liftweb.json._
import io.megam.util.Time
import org.joda.time.{DateTime, Period}
import org.joda.time.format.DateTimeFormat
import models.Constants.{JSON_CLAZ, REPORTSCLAZ, REPORT_RECENTLAUNCHESDOT}
import models.admin.{ReportInput, ReportResult}

class RecentLaunchesDot(ri: ReportInput) extends Reporter {

  def report: ValidationNel[Throwable, Option[ReportResult]] = {
    for {
      alt <-   build(ri.start_date, ri.end_date)  leftMap { err: NonEmptyList[Throwable] ⇒ err }
    } yield {
      ReportResult(REPORT_RECENTLAUNCHESDOT,
          new RecentLaunchesCounted(recent(alt)).toKeyList.asInstanceOf[Seq[models.tosca.KeyValueList]].some,
         REPORTSCLAZ, Time.now.toString).some

    }
  }

  def build(startdate: String, enddate: String): ValidationNel[Throwable,Seq[models.tosca.AssemblyResult]] = {
     for {
      a <- (models.tosca.Assembly.findByDateRange(startdate, enddate) leftMap { err: NonEmptyList[Throwable] ⇒ err })
    } yield  {
      a
    }

   }

   def reportFor(email: String, org: String): ValidationNel[Throwable, Option[ReportResult]] = none.successNel

   private def recent(abt: Seq[models.tosca.AssemblyResult]) = {
     for {
       ba <- (abt.groupBy(_.created_at.toString).map { case (k,v) => (k.toString -> v) }).some
     } yield {
       if (!ba.isEmpty) ListMap(ba.toSeq.sortWith(_._1 > _._1):_*) else ListMap()
     }
    }

}


case class RecentLaunchesCounted(recentMap: Option[ListMap[_ <: String,  Seq[models.tosca.AssemblyResult]]]) {

  private val X = "x"
  private val Y = "y"

  private lazy val upto = { if (recentMap.size >=5) 5  else (if (recentMap.size > 0) (recentMap.size - 1) else  0)  }

  private val RECENT  = recentMap.getOrElse(ListMap.empty).drop(upto).map(x => x._2).toSeq.flatten.map(y =>
    (y.name, y.status + "," + y.account_id +"," + y.created_at)
    )

  def toKeyList: models.tosca.KeyValueList = models.tosca.KeyValueList(RECENT.toMap)
}
