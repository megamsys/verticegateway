package models.addons

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import cache._
import db._
import models.Constants._
import models.json.tosca._
import models.tosca.{ KeyValueField, KeyValueList}
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
import controllers.stack.ImplicitJsonFormats
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset


/**
 * @author ranjitha
 *
 */
case class AddonsInput( provider_id: String, provider_name: String, options: models.tosca.KeyValueList)

case class AddonsResult(
    id: String,
    provider_id: String,
    account_id: String,
    provider_name: String,
    options: models.tosca.KeyValueList,
    json_claz: String,
    created_at: DateTime)

sealed class AddonsSacks extends CassandraTable[AddonsSacks, AddonsResult] with ImplicitJsonFormats {

  object id extends StringColumn(this)
  object provider_id extends StringColumn(this)
  object account_id extends StringColumn(this) with PrimaryKey[String]
  object provider_name extends StringColumn(this) with  PartitionKey[String]

  object options extends JsonListColumn[AddonsSacks, AddonsResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }
  object json_claz extends StringColumn(this)
  object created_at extends DateTimeColumn(this)

  def fromRow(row: Row): AddonsResult = {
    AddonsResult(
      id(row),
      provider_id(row),
      account_id(row),
      provider_name(row),
      options(row),
      json_claz(row),
      created_at(row))
  }
}

abstract class ConcreteAddons extends AddonsSacks with RootConnector {

  override lazy val tableName = "addons"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(adn: AddonsResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, adn.id)
     .value(_.provider_id, adn.provider_id)
      .value(_.account_id, adn.account_id)
      .value(_.provider_name, adn.provider_name)
      .value(_.options, adn.options)
      .value(_.json_claz, adn.json_claz)
      .value(_.created_at, adn.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def getRecords(email: String, name: String): ValidationNel[Throwable, Seq[AddonsResult]] = {
    val res = select.allowFiltering().where(_.account_id eqs email).and(_.provider_name eqs name).fetch()
    Await.result(res, 5.seconds).successNel
  }

  def deleteRecords(email: String): ValidationNel[Throwable, ResultSet] = {
    val res = delete.where(_.account_id eqs email).future()
    Await.result(res, 5.seconds).successNel
  }

}

object Addons extends ConcreteAddons {


  private def mkAddonsSack(email: String, input: String): ValidationNel[Throwable, AddonsResult] = {
    val adnInput: ValidationNel[Throwable, AddonsInput] = (Validation.fromTryCatchThrowable[AddonsInput, Throwable] {
      parse(input).extract[AddonsInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      ads <- adnInput
      uir <- (UID("adn").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {

      val bvalue = Set(email)
      val json = new AddonsResult(uir.get._1 + uir.get._2, ads.provider_id, email, ads.provider_name, ads.options, "Megam::Addons", DateHelper.now())
      json
    }
  }


  def create(email: String, input: String): ValidationNel[Throwable, Option[AddonsResult]] = {
    for {
      wa <- (mkAddonsSack(email, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (insertNewRecord(wa) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Addons.created success", Console.RESET))
      wa.some
    }
  }

  def findById(email: String, name: String): ValidationNel[Throwable, Seq[AddonsResult]] = {
    (getRecords(email, name) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(name, "Addons = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[AddonsResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[AddonsResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[AddonsResult]](new ResourceItemNotFound(name, "Addons = nothing found.")).toValidationNel
    }
  }

  def delete(email: String): ValidationNel[Throwable, Option[AddonsResult]] = {
    deleteRecords(email) match {
      case Success(value) => Validation.success[Throwable, Option[AddonsResult]](none).toValidationNel
      case Failure(err) => Validation.success[Throwable, Option[AddonsResult]](none).toValidationNel
    }
  }

}
