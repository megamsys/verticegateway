package models.tosca

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import cache._
import db._
import models.tosca._
import models.json.tosca._
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._
import models.base._
import wash._

import utils.DateHelper
import io.megam.util.Time
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.{DateTimeFormat,ISODateTimeFormat}

import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import scala.concurrent.{ Future => ScalaFuture }
import com.websudos.phantom.connectors.{ ContactPoint, KeySpaceDef }
import scala.concurrent.Await
import scala.concurrent.duration._
import com.websudos.phantom.iteratee.Iteratee
import controllers.stack.ImplicitJsonFormats

case class SensorsInput(account_id: String, sensor_type: String, assembly_id: String, assembly_name: String, assemblies_id: String,
                        node: String, system: String, status: String, source: String, message: String, audit_period_beginning: String,
                        audit_period_ending: String, audit_period_delta: String, metrics: models.tosca.MetricsList,
                        created_at: DateTime)

case class SensorsResult(id: String, account_id: String, sensor_type: String, assembly_id: String, assembly_name: String, assemblies_id: String,
                        node: String, system: String, status: String, source: String, message: String, audit_period_beginning: String,
                        audit_period_ending: String, audit_period_delta: String, metrics: models.tosca.MetricsList,
                        created_at: DateTime)

case class Metrics(metric_name: String, metric_value: String, metric_units: String, metric_type: String)

object SensorsResult {
  def apply(id: String, account_id: String, sensor_type: String, assembly_id: String, assembly_name: String, assemblies_id: String,
            node: String, system: String, status: String, source: String, message: String, audit_period_beginning: String,
            audit_period_ending: String, audit_period_delta: String, metrics: models.tosca.MetricsList) =
            new SensorsResult(id, account_id, sensor_type, assembly_id, assembly_name,  assemblies_id,
            node, system, status, source, message, audit_period_beginning, audit_period_ending, audit_period_delta,
            metrics, DateHelper.now())
}

sealed class SensorsSacks extends CassandraTable[SensorsSacks, SensorsResult] with ImplicitJsonFormats {

  object id extends StringColumn(this)
  object account_id extends StringColumn(this) with PartitionKey[String]
  object created_at extends DateTimeColumn(this) with PrimaryKey[DateTime]
  object assembly_id extends StringColumn(this) with PrimaryKey[String]
  object sensor_type extends StringColumn(this) with PrimaryKey[String]
  object assembly_name extends StringColumn(this)
  object assemblies_id extends StringColumn(this)
  object node extends StringColumn(this)
  object system extends StringColumn(this)
  object status extends StringColumn(this)
  object source extends StringColumn(this)
  object message extends StringColumn(this)
  object audit_period_beginning extends StringColumn(this)
  object audit_period_ending extends StringColumn(this)
  object audit_period_delta extends StringColumn(this)

  object metrics extends JsonListColumn[SensorsSacks, SensorsResult, Metrics](this) {
    override def fromJson(obj: String): Metrics = {
      JsonParser.parse(obj).extract[Metrics]
    }
    override def toJson(obj: Metrics): String = {
      compactRender(Extraction.decompose(obj))
    }
}


  def fromRow(row: Row): SensorsResult = {
    SensorsResult(
      id(row),
      account_id(row),
      sensor_type(row),
      assembly_id(row),
      assembly_name(row),
      assemblies_id(row),
      node(row),
      system(row),
      status(row),
      source(row),
      message(row),
      audit_period_beginning(row),
      audit_period_ending(row),
      audit_period_delta(row),
      metrics(row),
      created_at(row))
  }
}

abstract class ConcreteSensors extends SensorsSacks with RootConnector {

  override lazy val tableName = "sensors"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session


  def insertNewRecord(se: SensorsResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, se.id)
      .value(_.account_id, se.account_id)
      .value(_.sensor_type, se.sensor_type)
      .value(_.assembly_id, se.assembly_id)
      .value(_.assembly_name, se.assembly_name)
      .value(_.assemblies_id, se.assemblies_id)
      .value(_.node, se.node)
      .value(_.system, se.system)
      .value(_.status, se.status)
      .value(_.source, se.source)
      .value(_.message, se.message)
      .value(_.audit_period_beginning, se.audit_period_beginning)
      .value(_.audit_period_ending, se.audit_period_ending)
      .value(_.audit_period_delta, se.audit_period_delta)
      .value(_.metrics, se.metrics)
      .value(_.created_at, se.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }
}

object Sensors extends ConcreteSensors {

private def mkSensorsSack(email: String, input: String): ValidationNel[Throwable, SensorsResult] = {
  val ripNel: ValidationNel[Throwable, SensorsInput] = (Validation.fromTryCatchThrowable[SensorsInput, Throwable] {
    parse(input).extract[SensorsInput]
  } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

  for {
    ses <- ripNel
    uir <- (UID("sen").get leftMap { ut: NonEmptyList[Throwable] => ut })
  } yield {
    new SensorsResult(uir.get._1 + uir.get._2, ses.account_id, ses.sensor_type, ses.assembly_id, ses.assembly_name,
      ses.assemblies_id, ses.node, ses.system, ses.status, ses.source, ses.message, ses.audit_period_beginning,
      ses.audit_period_ending, ses.audit_period_delta, ses.metrics, DateHelper.now())
  }
}


def create(email: String, input: String): ValidationNel[Throwable, Option[SensorsResult]] = {
  for {
    se <- (mkSensorsSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
    set <- (insertNewRecord(se) leftMap { t: NonEmptyList[Throwable] => t })
  } yield {
    play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Sensors.created success", Console.RESET))
    se.some
  }
}

}
