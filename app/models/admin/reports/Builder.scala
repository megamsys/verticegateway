package models.admin.reports

import scalaz._
import Scalaz._
import scalaz.Validation.FlatMap._
import models.admin.ReportInput

trait Reporter { def report: ValidationNel[Throwable, Option[Reported]] }

object Builder {
  def apply(ri: ReportInput): Builder = new Builder(ri)
}

class Builder(ri: ReportInput) {

  val SALES     = "sales"

  private val GLOBAL_REPORTS = Map(SALES -> "model.admin.reports.Sales")

  //private val claz =  GLOBAL_REPORTS.get(ri.type_of).getOrElse("DefaultReporter")
  //private val constructor = classOf[claz].getConstructors()(0)
  //private val reporter: Reporter = constructor.newInstance(ri:_*).asInstanceOf[reportClaz]
  private val reporter: Reporter = new Sales(ri)

  def build(): ValidationNel[Throwable, String] = (reporter.report flatMap {x => "test".successNel })

  override def toString: String = "Builder: {" + ri.toString //+ "," + reportClaz + "}"

}
