package models.admin.reports

import scalaz._
import Scalaz._
import scalaz.Validation.FlatMap._
import models.admin.{ ReportInput, ReportResult }

trait Reporter {
  def report: ValidationNel[Throwable, Option[ReportResult]]
  def reportFor(email: String, org: String): ValidationNel[Throwable, Option[ReportResult]]
}

object Builder {
  def apply(ri: ReportInput): Builder = new Builder(ri)
}

class Builder(ri: ReportInput) {

  //When a new report type is needed add a constant type here.
  val SALES            = "sales"
  val LAUNCHES         = "launches"
  val BACKUPS          = "backups"
  val SNAPSHOTS        = "snapshots"


  val RECENTLAUNCHES   = "recentlaunches"
  val RECENTSIGNUPS    = "recentsignups"

  val USERDOT          = "userdot"
  val LAUNCHDOT        = "launchdot"
  val POPULARDOT       = "populardot"
  val POPULARAPPSDOT   = "popularappsdot"


  //When a new report is needed add a class that will be a reporter.
  private val GLOBAL_REPORTS = Map(SALES             -> "models.admin.reports.Sales",
                                   LAUNCHES          -> "models.admin.reports.Launches",
                                   BACKUPS           -> "models.admin.reports.Backups",
                                   SNAPSHOTS         -> "models.admin.reports.Snapshots",
                                   RECENTLAUNCHES    -> "models.admin.reports.RecentLaunchesDot",
                                   RECENTSIGNUPS     -> "models.admin.reports.RecentSignupsDot",
                                   USERDOT           -> "models.admin.reports.UsersDot",
                                   LAUNCHDOT         -> "models.admin.reports.LaunchesDot",
                                   POPULARDOT        -> "models.admin.reports.PopularDot",
                                   POPULARAPPSDOT    -> "models.admin.reports.PopularXyzDot"
                                  )

  private lazy val cls =  GLOBAL_REPORTS.get(ri.type_of).getOrElse("models.admin.reports.NoOp")

  private lazy val reporter: Reporter = {
    Class.forName(cls).getConstructor(Class.forName("models.admin.ReportInput")).newInstance(ri).asInstanceOf[Reporter]
  }

  //builds global report of everything
  def build: ValidationNel[Throwable, Option[ReportResult]] = reporter.report

  //build report and filter for an user.
  def buildFor(email: String, org: String): ValidationNel[Throwable, Option[ReportResult]] = reporter.reportFor(email, org)

}
