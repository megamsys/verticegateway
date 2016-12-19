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
import models.Constants.{JSON_CLAZ, REPORTSCLAZ, REPORT_USRDOT}
import models.admin.{ReportInput, ReportResult}
import io.megam.auth.stack.Role.{ADMIN}


class UsersDot(ri: ReportInput) extends Reporter {

  def report: ValidationNel[Throwable, Option[ReportResult]] = {
    for {
      alt <-   all  leftMap { err: NonEmptyList[Throwable] ⇒ err }
      adt <-   admin  leftMap { err: NonEmptyList[Throwable] ⇒ err }
    } yield {
      ReportResult(REPORT_USRDOT, new UserCounted(alt, adt).toKeyList.asInstanceOf[Seq[List[models.tosca.KeyValueField]]].some, REPORTSCLAZ, Time.now.toString).some
    }
  }

 def all: ValidationNel[Throwable, String] = {
    for {
     a <- (models.admin.Users.countAll leftMap { err: NonEmptyList[Throwable] ⇒ err })
   } yield a
  }

  def admin: ValidationNel[Throwable, String] = {
     for {
      a <- (models.admin.Users.countAdmin leftMap { err: NonEmptyList[Throwable] ⇒ err })
    } yield a
   }
}


case class UserCounted(all: String, admin: String) {
    private val X = "x"
    private val Y = "y"
    private val ALL = "all"


  def toKeyList: models.tosca.KeyValueList = models.tosca.KeyValueList(
    Map((X -> "usersdot" ),
        (Y -> "nos"),
        (ALL -> all),
        (ADMIN -> admin)))
}
