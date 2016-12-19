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
import models.Constants.{JSON_CLAZ, REPORTSCLAZ, REPORT_USRCOUNT}
import models.admin.{ReportInput, ReportResult}

class Users(ri: ReportInput) extends Reporter {

  def report: ValidationNel[Throwable, Option[ReportResult]] = {
    for {
      alt <-   all  leftMap { err: NonEmptyList[Throwable] ⇒ err }
      adt <-   admin  leftMap { err: NonEmptyList[Throwable] ⇒ err }
    } yield {
      ReportResult(REPORT_USRCOUNT, new UserCounted(alt, adt).toKeyList.asInstanceOf[Seq[List[models.tosca.KeyValueField]]].some, REPORTSCLAZ, Time.now.toString).some
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
    val X = "x"
    val Y = "y"
    val ALL = "all"
    val ADMIN = "admin"


  def toKeyList: models.tosca.KeyValueList = models.tosca.KeyValueList(
    Map((ALL -> all),
        (ADMIN -> admin)))
}
