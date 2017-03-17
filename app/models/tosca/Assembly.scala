package models.tosca

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


/**
 * @author rajthilak
 *
 */
case class Operation(operation_type: String, description: String, properties: models.tosca.KeyValueList, status: String)

case class AssemblyResult(id: String,
                          org_id: String,
                          account_id: String,
                          name: String,
                          components: models.tosca.ComponentLinks,
                          tosca_type: String,
                          policies: models.tosca.PoliciesList,
                          inputs: models.tosca.KeyValueList,
                          outputs: models.tosca.KeyValueList,
                          status: String,
                          state: String,
                          json_claz: String,
                          created_at: DateTime) {

    def isInvisible =   (STATUS_DESTROYED.contains(status) || STATUS_DESTROYED.contains(state))

                          }

sealed class AssemblySacks extends CassandraTable[AssemblySacks, AssemblyResult]  with ImplicitJsonFormats {

  object org_id extends StringColumn(this) with PartitionKey[String]
  object id extends StringColumn(this) with PrimaryKey[String]
  object created_at extends DateTimeColumn(this) with PrimaryKey[DateTime]
  object account_id extends StringColumn(this)
  object name extends StringColumn(this)
  object components extends ListColumn[AssemblySacks, AssemblyResult, String](this)
  object tosca_type extends StringColumn(this)

  object policies extends JsonListColumn[AssemblySacks, AssemblyResult, Policy](this) {
    override def fromJson(obj: String): Policy = {
      JsonParser.parse(obj).extract[Policy]
    }
    override def toJson(obj: Policy): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object inputs extends JsonListColumn[AssemblySacks, AssemblyResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object outputs extends JsonListColumn[AssemblySacks, AssemblyResult, KeyValueField](this) {
    override def fromJson(obj: String): KeyValueField = {
      JsonParser.parse(obj).extract[KeyValueField]
    }

    override def toJson(obj: KeyValueField): String = {
      compactRender(Extraction.decompose(obj))
    }
  }

  object status extends StringColumn(this)
  object state extends StringColumn(this)
  object json_claz extends StringColumn(this)

  def fromRow(row: Row): AssemblyResult = {
    AssemblyResult(
      id(row),
      org_id(row),
      account_id(row),
      name(row),
      components(row),
      tosca_type(row),
      policies(row),
      inputs(row),
      outputs(row),
      status(row),
      state(row),
      json_claz(row),
      created_at(row))
  }
}

abstract class ConcreteAssembly extends AssemblySacks with RootConnector {

  override lazy val tableName = "assembly"
  override implicit def space: KeySpace = scyllaConnection.space
  override implicit def session: Session = scyllaConnection.session

  def insertNewRecord(ams: AssemblyResult): ValidationNel[Throwable, ResultSet] = {
    val res = insert.value(_.id, ams.id)
      .value(_.org_id, ams.org_id)
      .value(_.account_id, ams.account_id)
      .value(_.name, ams.name)
      .value(_.components, ams.components)
      .value(_.tosca_type, ams.tosca_type)
      .value(_.policies, ams.policies)
      .value(_.inputs, ams.inputs)
      .value(_.outputs, ams.outputs)
      .value(_.status, ams.status)
      .value(_.state, ams.state)
      .value(_.json_claz, ams.json_claz)
      .value(_.created_at, ams.created_at)
      .future()
    Await.result(res, 5.seconds).successNel
  }

  def listRecords(org: String): ValidationNel[Throwable, Seq[AssemblyResult]] = {
    val res = select.allowFiltering().where(_.org_id eqs org).fetch()
    Await.result(res, 5.seconds).successNel
  }

  //Grand dump of all.
  def listAllRecords(): ValidationNel[Throwable, Seq[AssemblyResult]] = {
     val res = select.fetch()
    Await.result(res, 5.seconds).successNel
   }

   def countRecords(org: String): ValidationNel[Throwable, Option[Long]] = {
     val res = select.count.allowFiltering().where(_.org_id eqs org).one
      Await.result(res, 5.seconds).successNel
   }

   //Grand dump of all.
   def countAllRecords: ValidationNel[Throwable, Option[Long]] = {
      val res = select.count.one
     Await.result(res, 5.seconds).successNel
    }

  def dateRangeBy(startdate: String, enddate: String): ValidationNel[Throwable, Seq[AssemblyResult]] = {
      val starttime = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).parseDateTime(startdate);
      val endtime = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).parseDateTime(enddate);


     val res = select.allowFiltering().where(_.created_at gte starttime).and(_.created_at lte endtime).fetch()

    Await.result(res, 15.seconds).successNel
  }

    def dateRangeByFor(email: String, org: String, startdate: String, enddate: String): ValidationNel[Throwable, Seq[AssemblyResult]] = {
      val starttime = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).parseDateTime(startdate);
      val endtime = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).parseDateTime(enddate);

     val res = select.allowFiltering().where(_.org_id eqs org).and(_.created_at gte starttime).and(_.created_at lte endtime).fetch()
    Await.result(res, 5.seconds).successNel
  }

  def getRecord(id: String): ValidationNel[Throwable, Option[AssemblyResult]] = {
    val res = select.allowFiltering().where(_.id eqs id).and(_.created_at lte DateHelper.now()).one()
    Await.result(res, 5.seconds).successNel
  }

  def deleteRecords(org_id: String): ValidationNel[Throwable, ResultSet] = {
    val res = delete.where(_.org_id eqs org_id).future()
    Await.result(res, 5.seconds).successNel
  }

  def deleteRecordsById(org_id: String ,id: String, created_at: DateTime): ValidationNel[Throwable, ResultSet] = {
    val res = delete.where(_.org_id eqs org_id).and(_.created_at eqs created_at).and(_.id eqs id).future()
    Await.result(res, 5.seconds).successNel
  }

  def updateRecord(org_id: String, rip: AssemblyResult): ValidationNel[Throwable, ResultSet] = {
    val res = update.where(_.created_at eqs rip.created_at).and(_.id eqs rip.id).and(_.org_id eqs org_id)
      .modify(_.name setTo rip.name)
      .and(_.components setTo rip.components)
      .and(_.tosca_type setTo rip.tosca_type)
      .and(_.policies setTo rip.policies)
      .and(_.inputs setTo rip.inputs)
      .and(_.outputs setTo rip.outputs)
      .and(_.status setTo rip.status)
      .and(_.state setTo rip.state)
      .future()

    Await.result(res, 5.seconds).successNel
  }

}

case class Policy(name: String, ptype: String, resources: KeyValueList, rules: KeyValueList, properties: KeyValueList, status: String,
                 created_at: DateTime, updated_at: DateTime)

case class Assembly(name: String,
                    components: models.tosca.ComponentsList,
                    tosca_type: String,
                    policies: models.tosca.PoliciesList,
                    inputs: models.tosca.KeyValueList,
                    outputs: models.tosca.KeyValueList,
                    status: String,
                    state: String)

// The difference between Assembly and AssemblyUpdateInput is the `id` field
case class AssemblyUpdateInput(id: String,
                               name: String,
                               components: models.tosca.ComponentLinks,
                               tosca_type: String,
                               policies: models.tosca.PoliciesList,
                               inputs: models.tosca.KeyValueList,
                               outputs: models.tosca.KeyValueList,
                               status: String,
                               state: String) {
}

case class WrapAssemblyResult(thatGS: Option[AssemblyResult]) extends ImplicitJsonFormats  {
  val asm = thatGS.get
  val cattype = asm.tosca_type.split('.')(1)
  val domain = asm.inputs.find(_.key.equalsIgnoreCase(DOMAIN))
  val alma = asm.name + "." + domain.get.value //None is ignored here. dangerous.

}

object Assembly extends ConcreteAssembly {

  def findById(assemblyID: Option[List[String]]): ValidationNel[Throwable, AssemblyResults] = {
    (assemblyID map {
      _.map { asm_id =>
        (getRecord(asm_id) leftMap { t: NonEmptyList[Throwable] =>
          new ServiceUnavailableError(asm_id, (t.list.map(m => m.getMessage)).mkString("\n"))
        }).toValidationNel.flatMap { xso: Option[AssemblyResult] =>
          xso match {
            case Some(xs) => {
              Validation.success[Throwable, AssemblyResults](List(xs.some)).toValidationNel //screwy kishore, every element in a list ?
            }
            case None => {
              Validation.failure[Throwable, AssemblyResults](new ResourceItemNotFound(asm_id, "")).toValidationNel
            }
          }
        }
      } // -> VNel -> fold by using an accumulator or successNel of empty. +++ => VNel1 + VNel2
    } map {
      _.foldRight((AssemblyResults.empty).successNel[Throwable])(_ +++ _)
    }).head //return the folded element in the head.
  }

  def findByOrgId(org_id: String): ValidationNel[Throwable, Seq[AssemblyResult]] = {
    (listRecords(org_id) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(org_id, "Assembly = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[AssemblyResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[AssemblyResult]](nm).toValidationNel
      else
        Validation.success[Throwable, Seq[AssemblyResult]](List[AssemblyResult]()).toValidationNel
    }
  }

  def countAll: ValidationNel[Throwable, Option[Long]] = {
    (countAllRecords leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound("", "Assembly = nothing found.")
    }).toValidationNel.flatMap { nm: Option[Long] =>
      if (!nm.isEmpty)
        Validation.success[Throwable,Option[Long]](nm).toValidationNel
      else
        Validation.success[Throwable, Option[Long]](None).toValidationNel
    }
  }

  def countByOrgId(org_id: String): ValidationNel[Throwable, Option[Long]] = {
    (countRecords(org_id) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(org_id, "Assembly = nothing found.")
    }).toValidationNel.flatMap { nm: Option[Long] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Option[Long]](nm).toValidationNel
      else
        Validation.success[Throwable, Option[Long]](None).toValidationNel
    }
  }

  def listAll(): ValidationNel[Throwable, Seq[AssemblyResult]] = {
     (listAllRecords leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound("", "Assembly = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[AssemblyResult] =>
      if (!nm.isEmpty)
        Validation.success[Throwable, Seq[AssemblyResult]](nm).toValidationNel
      else
        Validation.failure[Throwable, Seq[AssemblyResult]](new ResourceItemNotFound("", "Assembly = nothing found.")).toValidationNel
     }
  }


  def findByDateRange(startdate: String, enddate: String): ValidationNel[Throwable, Seq[AssemblyResult]] = {
    dateRangeBy(startdate, enddate) match {
      case Success(value) => Validation.success[Throwable, Seq[AssemblyResult]](value).toValidationNel
      case Failure(err) => Validation.success[Throwable, Seq[AssemblyResult]](List()).toValidationNel
    }
  }

  def findByDateRangeFor(email: String, org: String, startdate: String, enddate: String): ValidationNel[Throwable, Seq[AssemblyResult]] = {
    dateRangeByFor(email, org, startdate, enddate) match {
      case Success(value) => Validation.success[Throwable, Seq[AssemblyResult]](value).toValidationNel
      case Failure(err) => Validation.success[Throwable, Seq[AssemblyResult]](List()).toValidationNel
    }
  }

  def update(org_id: String, input: String): ValidationNel[Throwable, Option[AssemblyResult]] = {
    for {
      gs <- (updateAssemblySack(org_id, input) leftMap { err: NonEmptyList[Throwable] => err })
      set <- (updateRecord(org_id, gs.get) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.YELLOW, Console.BOLD, "Assembly","|÷| ✔", Console.RESET))
      gs
    }
  }

  def hardDeleteByOrgId(org_id: String): ValidationNel[Throwable, Option[AssemblyResult]] = {
    deleteRecords(org_id) match {
      case Success(value) => Validation.success[Throwable, Option[AssemblyResult]](none).toValidationNel
      case Failure(err) => Validation.success[Throwable, Option[AssemblyResult]](none).toValidationNel
    }
  }

  def softDeleteByOrgId(org_id: String): ValidationNel[Throwable, AssemblyResult] = {
    for {
      wa <- (findByOrgId(org_id) leftMap { t: NonEmptyList[Throwable] => t })
      df <- deleteFound(org_id, "", wa)
    } yield df
  }

  def hardDeleteById(org_id: String, id: String, created_at: DateTime): ValidationNel[Throwable, Option[AssemblyResult]] = {
    deleteRecordsById(org_id, id, created_at) match {
      case Success(value) => {
        play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.RED, Console.BOLD, "Assembly","|-| ✔", Console.RESET))
        Validation.success[Throwable, Option[AssemblyResult]](none).toValidationNel
      }
      case Failure(err) => Validation.success[Throwable, Option[AssemblyResult]](none).toValidationNel
    }
  }

  def softDeleteById(id: String, asbs_id: String, email: String): ValidationNel[Throwable, AssemblyResult] = {
    for {
      wa <- (findById(List(id).some) leftMap { t: NonEmptyList[Throwable] => t })
      df <- deleteFound(email, asbs_id, wa.map(_.get)) //TO-DO: chance for crashing ?
    } yield {
      df
    }
  }


  private def deleteFound(email: String, asbs_id: String, an: Seq[AssemblyResult]) = {
      val output = (an.map { asa =>
           if (!asa.isInvisible)
             dePub(email, asbs_id, asa)
          else
             asa.successNel[Throwable]
      })

      if (!output.isEmpty)
         output.head
      else
        AssemblyResult("","","","", models.tosca.ComponentLinks.empty,"",
          models.tosca.PoliciesList.empty, models.tosca.KeyValueList.empty,
          models.tosca.KeyValueList.empty, "", "", "", DateHelper.now()).successNel

   }

  private def dePub(email: String, asbs_id: String,  wa: AssemblyResult): ValidationNel[Throwable, AssemblyResult] = {
    models.base.Requests.createAndPub(email, RequestInput(email, asbs_id, wa.tosca_type, "", DELETE, STATE).json)
    wa.successNel[Throwable]
  }

  private def updateAssemblySack(org_id: String, input: String): ValidationNel[Throwable, Option[AssemblyResult]] = {
    val ripNel: ValidationNel[Throwable, AssemblyUpdateInput] = (Validation.fromTryCatchThrowable[AssemblyUpdateInput, Throwable] {
      parse(input).extract[AssemblyUpdateInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel //capture failure

    for {
      rip <- ripNel
      asm_collection <- (Assembly.findById(List(rip.id).some) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      val asm = asm_collection.head
      val json = AssemblyResult(rip.id, org_id, asm.get.account_id, asm.get.name, asm.get.components, asm.get.tosca_type, rip.policies, rip.inputs,
         rip.outputs,
         NilOrNot(rip.status, asm.get.status),
         NilOrNot(rip.state, asm.get.state),
         asm.get.json_claz,
         asm.get.created_at)
      json.some
    }
  }

  private def NilOrNot(rip: String, aor: String): String = {
    rip == null || rip == "" match {
      case true => return aor
      case false => return rip
    }
  }

}

object AssemblysList extends ConcreteAssembly {

  implicit def AssemblysListsSemigroup: Semigroup[AssemblysLists] = Semigroup.instance((f1, f2) => f1.append(f2))

  def apply(assemblyList: List[Assembly]): AssemblysList = { assemblyList }

  def createLinks(authBag: Option[io.megam.auth.stack.AuthBag], input: AssemblysList): ValidationNel[Throwable, AssemblysLists] = {
    val res = (input map {
      asminp => (create(authBag, asminp))
    }).foldRight((AssemblysLists.empty).successNel[Throwable])(_ +++ _)
      res.getOrElse(new ResourceItemNotFound(authBag.get.email, "assembly = ah. ouh. for some reason.").failureNel[AssemblysLists])
      res
  }


  def create(authBag: Option[io.megam.auth.stack.AuthBag], input: Assembly): ValidationNel[Throwable, AssemblysLists] = {
    for {
      ogsi <- mkAssemblySack(authBag, input) leftMap { err: NonEmptyList[Throwable] => err }
      set <- (insertNewRecord(ogsi.get) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      play.api.Logger.warn(("%s%s%-20s%s%s").format(Console.GREEN, Console.BOLD, "Assembly","|+| ✔", Console.RESET))
      nels(ogsi)
    }
  }

  def atQuotaUpdate(email: String, asm: Assembly, asm_id: String): ValidationNel[Throwable, QuotasResult] = {
    val quota_id = asm.inputs.find(_.key.equalsIgnoreCase("quota_id")).getOrElse(models.tosca.KeyValueField.empty).value
    val quo = QuotasUpdateInput(quota_id, email, null, asm_id, null, "", "")

    if (quota_id != "") {
      models.billing.Quotas.update(email, compactRender(Extraction.decompose(quo)))
    } else {
      QuotasResult(quota_id, "", email, models.tosca.KeyValueList.empty, asm_id, models.tosca.KeyValueList.empty, "","", "", DateHelper.now(), DateHelper.now()).successNel
    }
  }

  private def mkAssemblySack(authBag: Option[io.megam.auth.stack.AuthBag], rip: Assembly): ValidationNel[Throwable, Option[AssemblyResult]] = {
    var outlist = rip.outputs
    for {
      uir <- (UID("asm").get leftMap { ut: NonEmptyList[Throwable] => ut })
      com <- (ComponentsList.createLinks(authBag, rip.components, (uir.get._1 + uir.get._2)) leftMap { t: NonEmptyList[Throwable] => t })
      quo <- (atQuotaUpdate(authBag.get.email, rip, (uir.get._1 + uir.get._2)) leftMap { t: NonEmptyList[Throwable] => t })
    } yield {
      var components_links = new ListBuffer[String]()
      if (com.size > 1) {
        for (component <- com) {
          component match {
            case Some(value) => components_links += value.id
            case None => components_links
          }
        }
      }
      val json = AssemblyResult(uir.get._1 + uir.get._2, authBag.get.org_id, authBag.get.email, rip.name, components_links.toList, rip.tosca_type, rip.policies, rip.inputs, outlist, rip.status, rip.state, "Megam::Assembly", DateHelper.now())
      json.some
    }
  }
}
