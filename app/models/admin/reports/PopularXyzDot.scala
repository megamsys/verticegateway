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
import models.Constants.{JSON_CLAZ, REPORTSCLAZ, REPORT_POPAPPDOT,  REPORT_CATEGORYMAP}
import models.admin.{ReportInput, ReportResult}

class PopularXyzDot(ri: ReportInput) extends Reporter {


  def report: ValidationNel[Throwable, Option[ReportResult]] = {
    for {
      alt <-   build(ri.start_date, ri.end_date)  leftMap { err: NonEmptyList[Throwable] ⇒ err }
      pdt <-   popular(alt.map(_.get)).successNel leftMap { err: NonEmptyList[Throwable] ⇒ err }
    } yield {
      ReportResult(REPORT_POPAPPDOT,
          new PopularXyzCounted(pdt).toKeyList.asInstanceOf[Seq[models.tosca.KeyValueList]].some,
         REPORTSCLAZ, Time.now.toString).some

    }
  }

  def build(startdate: String, enddate: String): ValidationNel[Throwable, List[Option[models.tosca.ComponentResult]]] = {
     for {
      a <- (models.tosca.Assembly.findByDateRange(startdate, enddate) leftMap { err: NonEmptyList[Throwable] ⇒ err })
      c <- (models.tosca.Component.findById(Option(a.foldRight(List[String]())(_.components ++ _))))
    } yield {
      val f = REPORT_CATEGORYMAP.get(ri.category).getOrElse(REPORT_CATEGORYMAP.get("application").getOrElse(List()))
      c.filter { cf => (f.filter(x => cf.map(_.tosca_type.contains(x)).getOrElse(false)).size > 0) }
    }
   }

   def reportFor(email: String, org: String): ValidationNel[Throwable, Option[ReportResult]] = none.successNel


  private def popular(abt: List[models.tosca.ComponentResult]) = {
    for {
      ba <- (abt.groupBy(_.tosca_type).map { case (k,v) => (k -> v.size.toInt) }).some
    } yield {
      if (!ba.isEmpty) ListMap(ba.toSeq.sortWith(_._1 > _._1):_*) else ListMap()
    }
   }

}


case class PopularXyzCounted(popularMap: Option[ListMap[_ <: String, Int]]) {

  private val POPULAR  = popularMap.getOrElse(ListMap.empty)

  private lazy val upto = { if (POPULAR.size >=5) 5  else POPULAR.size  }

  private val popular  = POPULAR.take(upto).map(x => (x._1, x._2.toString))

  def toKeyList: models.tosca.KeyValueList = models.tosca.KeyValueList(Map[String,String]() ++ popular.toMap)
}
