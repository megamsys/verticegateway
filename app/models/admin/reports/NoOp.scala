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
import models.tosca.KeyValueList
import models.Constants.{REPORT_NOOP, REPORTSCLAZ}
import models.admin.{ReportInput, ReportResult}

//NoOp reporter. does nothing.
class NoOp(ri: ReportInput) extends Reporter {

  def report: ValidationNel[Throwable, Option[ReportResult]] = {
      ReportResult(REPORT_NOOP, KeyValueList.empty.asInstanceOf[Seq[KeyValueList]].some, REPORTSCLAZ,  Time.now.toString).some.successNel
    }
}
