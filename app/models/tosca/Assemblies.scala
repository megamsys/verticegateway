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
import models.Constants._
import models.json.tosca._
import models.json.tosca.carton._
import models.base.RequestInput
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


/**
 * @author rajthilak
 *
 */
case class AssembliesInput(name: String, org_id: String, assemblies: models.tosca.AssemblysList, inputs: KeyValueList) {

  val inputsMap = KeyValueList.toMap(inputs)

  def number_of_units: Int =   inputsMap.getOrElse("number_of_units", "0").toInt

  def nameOverriden(name: String): String =  { inputsMap.getOrElse(name, name) }
}

case class KeyValueField(key: String, value: String) {
  val json = "{\"key\":\"" + key + "\",\"value\":\"" + value + "\"}"

  def toJValue: JValue = {
    import net.liftweb.json.scalaz.JsonScalaz.toJSON
    val preser = new KeyValueFieldSerialization()
    toJSON(this)(preser.writer)
  }

  def toJson(prettyPrint: Boolean = false): String = if (prettyPrint) {
    prettyRender(toJValue)
  } else {
    compactRender(toJValue)
  }

}

object KeyValueField {
  def empty: KeyValueField = new KeyValueField(new String(), new String())

  def fromJValue(jValue: JValue)(implicit charset: Charset = UTF8Charset): Result[KeyValueField] = {
    import net.liftweb.json.scalaz.JsonScalaz.fromJSON
    val preser = new KeyValueFieldSerialization()
    fromJSON(jValue)(preser.reader)
  }

  def fromJson(json: String): Result[KeyValueField] = (Validation.fromTryCatchThrowable[net.liftweb.json.JValue, Throwable] {
    parse(json)
  } leftMap { t: Throwable =>
    UncategorizedError(t.getClass.getCanonicalName, t.getMessage, List())
  }).toValidationNel.flatMap { j: JValue => fromJValue(j) }

}

case class AssembliesResult(id: String,
    org_id: String,
    name: String,
    assemblies: models.tosca.AssemblyLinks,
    inputs: KeyValueList,
    json_claz: String,
    created_at: DateTime) {
}

object AssembliesResult {
  def apply(id: String, org_id: String, name: String, assemblies: models.tosca.AssemblyLinks, inputs: KeyValueList) = new AssembliesResult(id, org_id, name, assemblies, inputs, "Megam::Assemblies", DateHelper.now())
}

sealed class AssembliesSacks extends CassandraTable[AssembliesSacks, AssembliesResult] with ImplicitJsonFormats {

  object id extends StringColumn(this) with PrimaryKey[String]
  object org_id extends StringColumn(this) with PartitionKey[String]
  object created_at extends DateTimeColumn(this) with PrimaryKey[DateTime]

  object name extends StringColumn(this)
  object assemblies extends ListColumn[AssembliesSacks, AssembliesResult, String](this)

  object inputs extends JsonListColumn[AssembliesSacks, AssembliesResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object json_claz extends StringColumn(this)


  def fromRow(row: Row): AssembliesResult = {
    AssembliesResult(
      id(row),
      org_id(row),
      name(row),
      assemblies(row),
      inputs(row),
      json_claz(row),
      created_at(row))
  }
}

abstract class ConcreteAssemblies extends AssembliesSacks with RootConnector {

  override lazy val tableName = "assemblies"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(ams: AssembliesResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, ams.id)
      .value(_.org_id, ams.org_id)
      .value(_.name, ams.name)
      .value(_.assemblies, ams.assemblies)
      .value(_.inputs, ams.inputs)
      .value(_.json_claz, ams.json_claz)
      .value(_.created_at, ams.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(email: String, org: String): ValidationNel[Throwable, Seq[AssembliesResult]] = {
    val res = select.where(_.org_id eqs org).fetch()
    Await.result(res, 5.seconds).successNel
  }

  def dateRangeBy(startdate: String, enddate: String): ValidationNel[Throwable, Seq[AssembliesResult]] = {
    val starttime = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).parseDateTime(startdate);
    val endtime = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).parseDateTime(enddate);

    val res = select.allowFiltering().where(_.created_at gte starttime).and(_.created_at lte endtime).fetch()
    Await.result(res, 5.seconds).successNel
  }

  //Grand dump of all.
  def listAllRecords(): ValidationNel[Throwable, Seq[AssembliesResult]] = {
     val res = select.fetch()
    Await.result(res, 5.seconds).successNel
   }


  def getRecord(id: String): ValidationNel[Throwable, Option[AssembliesResult]] = {
    val res = select.allowFiltering().where(_.id eqs id).and(_.created_at lte DateHelper.now()).one()  
    Await.result(res, 5.seconds).successNel
  }

}

case class WrapAssembliesResult(thatGS: Option[AssembliesResult], idPair: Map[String, String]) extends  ImplicitJsonFormats {

  val ams = thatGS

  def cattype = idPair.map(x => x._2.split('.')(1)).head
}


object Assemblies extends ConcreteAssemblies {

  private def mkAssembliesSack(authBag: Option[io.megam.auth.stack.AuthBag], input: AssembliesInput): ValidationNel[Throwable, WrapAssembliesResult] = {
    val inp: ValidationNel[Throwable, AssembliesInput] = input.successNel[Throwable]

    for {
      iip <- inp
      ams <- (AssemblysList.createLinks(authBag, iip.assemblies) leftMap { t: NonEmptyList[Throwable] => t })
      uir <- (UID("ams").get leftMap { ut: NonEmptyList[Throwable] => ut })
    } yield {
      val asml = ams.flatMap { assembly => nels({ assembly.map { a => (a.id, a.tosca_type) } }) }
      val asmlist = asml.toList.filterNot(_.isEmpty)
      new WrapAssembliesResult((AssembliesResult(uir.get._1 + uir.get._2, iip.org_id, iip.name, asmlist.map(_.get._1), iip.inputs)).some, asmlist.map(_.get).toMap)
    }
  }

  def create(authBag: Option[io.megam.auth.stack.AuthBag], input: AssembliesInput): ValidationNel[Throwable, AssembliesResult] = {
    for {
      wa <- (mkAssembliesSack(authBag, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (insertNewRecord(wa.thatGS.get) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s").format(Console.WHITE, Console.BOLD, "Assemblies.created success", Console.RESET))
      pub(authBag.get.email, wa)
      wa.ams.get
    }
  }

   def findByDateRange(startdate: String, enddate: String): ValidationNel[Throwable, Seq[AssembliesResult]] = {
     (dateRangeBy(startdate, enddate) leftMap { t: NonEmptyList[Throwable] =>
       new ResourceItemNotFound("", "Assemblies = nothing found.")
     }).toValidationNel.flatMap { nm: Seq[AssembliesResult] =>
         Validation.success[Throwable, Seq[AssembliesResult]](nm).toValidationNel
     }
    }

  def findById(assembliesID: Option[List[String]]): ValidationNel[Throwable, AssembliesResults] = {
    (assembliesID map {
      _.map { asm_id =>
        (getRecord(asm_id) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(asm_id, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[AssembliesResult] =>
          xso match {
            case Some(xs) => {
              play.api.Logger.warn(("%s%s%-20s%s").format(Console.GREEN, Console.BOLD, "Assemblies."+asm_id + " successfully", Console.RESET))
              Validation.success[Throwable, AssembliesResults](List(xs.some)).toValidationNel //screwy kishore, every element in a list ?
            }
            case None => {
              Validation.failure[Throwable, AssembliesResults](new ResourceItemNotFound(asm_id, "")).toValidationNel
            }
          }
        }
      }
    } map {
      _.foldRight((AssembliesResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head.
  }


  def findByEmail(email: String, org: String): ValidationNel[Throwable, Seq[AssembliesResult]] = {
    (listRecords(email, org) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(email, "Assemblies = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[AssembliesResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[AssembliesResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[AssembliesResult]](new ResourceItemNotFound(email, "Assemblies = nothing found.")).toValidationNel
    }

  }

  /* Lets clean it up in 2.0 using Messageable  */
  private def pub(email: String, wa: WrapAssembliesResult): ValidationNel[Throwable, AssembliesResult] = {
    models.base.Requests.createAndPub(email, RequestInput(email, wa.ams.get.id, wa.cattype, "", CREATE, STATE).json)
    wa.ams.get.successNel[Throwable]
  }
}
