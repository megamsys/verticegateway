package models.admin.reports

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import models.admin.ReportInput

case class ReportResult(id: String, data: String, created_at: String)

class Sales(ri: ReportInput) extends Reporter {

  def report: ValidationNel[Throwable, Option[Reported]] = {
    for {
      abt <-   build(ri.start_date, ri.end_date)  leftMap { err: NonEmptyList[Throwable] ⇒ err }
      sal <-   aggregate(abt).successNel
    } yield {
      //sal
      Reported("DUMMY").some
    }
  }

 def build(starttime: String, endtime: String): ValidationNel[Throwable,
                Tuple2[Seq[models.tosca.AssemblyResult],
                Seq[models.billing.BilledhistoriesResult]]] = {
   for {
     a <- (models.tosca.Assembly.listAll leftMap { err: NonEmptyList[Throwable] ⇒ err })
     b <- (models.billing.Billedhistories.listAll leftMap { err: NonEmptyList[Throwable] ⇒ err })
   } yield {
       (a, b)
    }
  }

  def aggregate(abt: Tuple2[Seq[models.tosca.AssemblyResult], Seq[models.billing.BilledhistoriesResult]]) = {
   for {
     ba <- abt._2.groupBy(_.assembly_id).map { case (k,v) => BillingAggregate(k,v) }
     sa <-  SalesAggregate(abt._1, ba).some
    } yield {
      sa.aggregate
   }
  }
}


case class BillingAggregate(aid: String, b:  Seq[models.billing.BilledhistoriesResult]) {

    private lazy val start_dates = b.map(_.start_date.toString)
    lazy val start_date = start_dates.sortBy({r => r}).head

    private lazy val end_dates = b.map(_.end_date.toString)
    lazy val end_date = end_dates.sortBy({r => r}).head

    lazy val sum = b.map(_.billing_amount.toInt).sum

    override def toString() = "[" + aid + " sales from " + start_date + " to " + end_date + " is:"+ sum +"]";
}

case class SalesResult( asm_id: String, asm_name: String, status: String, state: String,startdate: String, enddate: String, cost: String)

case class SalesAggregate(als: Seq[models.tosca.AssemblyResult], bh: BillingAggregate) {
  lazy val aggregate = als.map(al =>  {
      SalesResult( al.id, al.name,al.status, al.state, bh.start_date, bh.end_date, bh.sum.toString)
   })

}
