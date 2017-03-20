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
import models.Constants.{JSON_CLAZ, REPORTSCLAZ, REPORT_RECENTSIGNUPSDOT}
import io.megam.auth.stack.AccountResult
import models.admin.{ReportInput, ReportResult}

class RecentSignupsDot(ri: ReportInput) extends Reporter {

  def report: ValidationNel[Throwable, Option[ReportResult]] = {
    for {
      alt <-   build(ri.start_date, ri.end_date)  leftMap { err: NonEmptyList[Throwable] ⇒ err }
      pdt <-   recent(alt).successNel leftMap { err: NonEmptyList[Throwable] ⇒ err }
    } yield {
      ReportResult(REPORT_RECENTSIGNUPSDOT,
          new RecentSignupsCounted(pdt).toKeyList.asInstanceOf[Seq[models.tosca.KeyValueList]].some,
         REPORTSCLAZ, Time.now.toString).some

    }
  }

  def build(startdate: String, enddate: String): ValidationNel[Throwable, Seq[AccountResult]] = {
     for {
      a <- (models.base.Accounts.list leftMap { err: NonEmptyList[Throwable] ⇒ err })
    } yield a
   }

   def reportFor(email: String, org: String): ValidationNel[Throwable, Option[ReportResult]] = none.successNel


  private def recent(abt: Seq[AccountResult]) = {
    for {
      ba <- (abt.groupBy(_.dates.created_at).map { case (k,v) => (k -> v) }).some
    } yield {
      if (!ba.isEmpty) ListMap(ba.toSeq.sortWith(_._1 > _._1):_*) else ListMap()
    }
   }

}


case class RecentSignupsCounted(popularMap: Option[ListMap[_ <: String, Seq[io.megam.auth.stack.AccountResult]]]) {

  private val RECENT  = popularMap.getOrElse(ListMap.empty)

  private lazy val upto = { if (RECENT.size >=5) 5  else RECENT.size  }

  private val recent  = RECENT.take(upto).map(x => x._2).toSeq.flatten.map{y =>
      (y.email, (if (!Option(y.states.active).getOrElse("").isEmpty) models.Constants.ACTIVE else models.Constants.NOT_ACTIVE) + "," + y.dates.created_at)}

  def toKeyList: models.tosca.KeyValueList = models.tosca.KeyValueList(recent.toMap)

}
