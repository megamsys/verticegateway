package models.user

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps
import scala.collection.mutable.ListBuffer

import cache._
import db._
import models.Constants._
import models.json.tosca._
import models.base.RequestInput
import io.megam.auth.funnel.FunnelErrors._
import models.billing.{ QuotasResult, QuotasUpdateInput }

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._

import utils.DateHelper
import io.megam.util.Time
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.{DateTimeFormat,ISODateTimeFormat}

import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import controllers.stack.ImplicitJsonFormats



case class LaunchesPtrResult(id: String, account_id: String, ptr: Long, created_at: DateTime)

sealed class LaunchPtrSacks extends CassandraTable[LaunchPtrSacks, LaunchesPtrResult]  with ImplicitJsonFormats {

  object id extends StringColumn(this) with PrimaryKey[String]
  object account_id extends StringColumn(this)  with PrimaryKey[String]
  object ptr extends LongColumn(this)
  object created_at extends DateTimeColumn(this)

  def fromRow(row: Row): LaunchesPtrResult = {
    LaunchesPtrResult(
      id(row),
      account_id(row),
      ptr(row),
      created_at(row))
  }
}

abstract class ConcreteLaunchesPtr extends LaunchPtrSacks with RootConnector {

  override lazy val tableName = "launchptr"

  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(lpr: LaunchesPtrResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, lpr.id)
      .value(_.account_id, lpr.account_id)
      .value(_.ptr, lpr.ptr)
      .value(_.created_at, lpr.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def listRecord(account_id: String): ValidationNel[Throwable, Option[LaunchesPtrResult]] = {
    val res = select.where(_.account_id eqs account_id).one
    Await.result(res, 5.seconds).successNel
  }

  def updateRecord(account_id: String, rip: LaunchesPtrResult): ValidationNel[Throwable, ResultSet] = {
    val res = update.where(_.account_id eqs rip.account_id).and(_.id eqs rip.id)
      .modify(_.ptr setTo rip.ptr)
      .future()

    Await.result(res, 5.seconds).successNel
  }
}


object LaunchPtr extends ConcreteLaunchesPtr {

  def count(account_id: String): ValidationNel[Throwable, Option[Long]] = {
    for {
      rec <- findById(account_id)
      res <- createOrUpdate(account_id, rec)
    } yield res
  }

  private def mkLaunchPtrSack(account_id: String):  ValidationNel[Throwable, LaunchesPtrResult] = {
    for {
      uir <- (UID("lpt").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      LaunchesPtrResult(uir.get._1 + uir.get._2, account_id, 1, DateHelper.now())
    }
  }

  private def findById(account_id: String): ValidationNel[Throwable, Option[LaunchesPtrResult]] = {
    (listRecord(account_id) match {
      case Success(succ) =>  Validation.success[Throwable, Option[LaunchesPtrResult]](succ).toValidationNel
      case Failure(_)     =>   Validation.success[Throwable, Option[LaunchesPtrResult]](None).toValidationNel
    })
  }

  private def create(account_id: String): ValidationNel[Throwable, Option[Long]] = {
    for {
      mkl  <- (mkLaunchPtrSack(account_id) leftMap { ut: NonEmptyList[Throwable] => ut })
      pset <- (insertNewRecord(mkl) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.GREEN, Console.BOLD, "LaunchPtr","|+| ✔", Console.RESET))
      mkl.ptr.some
    }
  }

  private def update(account_id: String, old: LaunchesPtrResult): ValidationNel[Throwable, Option[Long]] = {
    val newRes = Some(old).map(o => LaunchesPtrResult(o.id, o.account_id, o.ptr + 1, o.created_at)).get

    for {
      pset <- (updateRecord(account_id, newRes) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.YELLOW, Console.BOLD, "LaunchPtr","|÷| ✔", Console.RESET))
      newRes.ptr.some
    }
  }

  private def createOrUpdate(account_id: String, found: Option[LaunchesPtrResult]) = {
    (found match {
      case Some(succ) =>  update(account_id, succ)
      case None       =>  create(account_id)
    })
  }
}
