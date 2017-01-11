package models.admin

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import cache._
import db._
import io.megam.auth.funnel.FunnelErrors._
import controllers.Constants._

import io.megam.common.uid.UID
import io.megam.util.Time
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import controllers.stack.ImplicitJsonFormats
import models.admin.reports.Builder

//The input, and report classes for any report.
case class ReportInput(type_of: String, start_date: String, end_date: String, category: String, group: String)

case class ReportResult(id: String, data: Option[Seq[models.tosca.KeyValueList]], json_claz: String, created_at: String)

//A generic ability to generate any report.
//No change is needed here, all we need to add is a new Reporter under admin/reports.
//Right now we send back Sales, Machines
object Reports extends ImplicitJsonFormats {

  private def mkReportInput(input: String): ValidationNel[Throwable, ReportInput] = {
   (Validation.fromTryCatchThrowable[ReportInput, Throwable] {
      parse(input).extract[ReportInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
  }

  def create(input: String): ValidationNel[Throwable, Option[ReportResult]] = {
    for {
      r   <- mkReportInput(input)
      res <- Builder(r).build
    } yield {
      res
    }
  }

  def createFor(email: String, org: String, input: String): ValidationNel[Throwable, Option[ReportResult]] = {
    for {
      r   <- mkReportInput(input)
      res <- Builder(r).buildFor(email, org)
    } yield {
      res
    }
  }

}
