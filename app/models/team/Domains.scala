package models.team

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import cache._
import db._
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._

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

case class DomainsInput(name: String) {
  val json = "{\"name\":\"" + name + "\"}"
}

case class DomainsResult(
  id: String,
  org_id: String,
  name: String,
  json_claz: String,
  created_at: DateTime)

sealed class DomainsT extends CassandraTable[DomainsT, DomainsResult] {

  object id extends StringColumn(this) with PrimaryKey[String]
  object org_id extends StringColumn(this) with PartitionKey[String]
  object name extends StringColumn(this)
  object json_claz extends StringColumn(this)
  object created_at extends DateTimeColumn(this)

  override def fromRow(r: Row): DomainsResult = {
    DomainsResult(
      id(r),
      org_id(r),
      name(r),
      json_claz(r),
      created_at(r))
  }
}

abstract class ConcreteDmn extends DomainsT with ScyllaConnector {

  override lazy val tableName = "domains"

  def insertNewRecord(d: DomainsResult): ResultSet = {
    val res = insert.value(_.id, d.id)
      .value(_.org_id, d.org_id)
      .value(_.name, d.name)
      .value(_.json_claz, d.json_claz)
      .value(_.created_at, d.created_at)
      .future()
    Await.result(res, 5.seconds)
  }

  def listRecords(org_id: String): ValidationNel[Throwable, Seq[DomainsResult]] = {
    val resp = select.allowFiltering().where(_.org_id eqs org_id).fetch()
    (Await.result(resp, 5.seconds)).successNel
  }

}

object Domains extends ConcreteDmn with ImplicitJsonFormats {


  private def dmnNel(input: String): ValidationNel[Throwable, DomainsInput] = {
    (Validation.fromTryCatchThrowable[DomainsInput, Throwable] {
      parse(input).extract[DomainsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
  }

  private def domainsSet(id: String, org_id: String, c: DomainsInput): ValidationNel[Throwable, DomainsResult] = {
    (Validation.fromTryCatchThrowable[DomainsResult, Throwable] {
      DomainsResult(id, org_id, c.name, "Megam::Domains", DateHelper.now())
    } leftMap { t: Throwable => new MalformedBodyError(c.json, t.getMessage) }).toValidationNel
  }


  def create(org_id: String, input: String): ValidationNel[Throwable, DomainsResult] = {
    for {
      c <- dmnNel(input)
      uir <- (UID("DMN").get leftMap { u: NonEmptyList[Throwable] => u })
      dmn <- domainsSet(uir.get._1 + uir.get._2, org_id, c)
    } yield {
      insertNewRecord(dmn)
      dmn
    }
  }

  def findByOrgId(authBag: Option[io.megam.auth.stack.AuthBag]): ValidationNel[Throwable, Seq[DomainsResult]] = {
    (listRecords(authBag.get.org_id) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(authBag.get.email, "Domains = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[DomainsResult] =>
        Validation.success[Throwable, Seq[DomainsResult]](nm).toValidationNel
    }
  }

}
