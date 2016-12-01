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

import models.admin.reports.Reported
import models.admin.reports.Builder

case class ReportInput(type_of: String, start_date: String, end_date: String, category: String, group: String)

object Reports {

  implicit val formats = DefaultFormats

  private def mkReportInput(input: String): ValidationNel[Throwable, ReportInput] = {
    (Validation.fromTryCatchThrowable[ReportInput, Throwable] {
      parse(input).extract[ReportInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
  }

  def create(input: String): ValidationNel[Throwable, Reported] = {
    for {
      r   <- mkReportInput(input)
      res <- new Builder(r).build
  //    evn <- Events(ast.id, EVENTUSER, Events.ONBOARD, Map(EVTEMAIL -> ast.email)).createAndPub()
    } yield {
      res
    }
  }

}
