package models.base

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



case class SshKeysInput(name: String, privatekey: String, publickey: String) {
  val json = "{\"name\":\"" + name + "\",\"privatekey\":\"" + privatekey + "\",\"publickey\":\"" + publickey + "\"}"
}


case class SshKeysResult(
  id: String,
  org_id: String,
  name: String,
  privatekey: String,
  publickey: String,
  json_claz: String,
  created_at: DateTime)


sealed class SshKeysT extends CassandraTable[SshKeysT, SshKeysResult] {

  object id extends StringColumn(this) with PrimaryKey[String]
  object org_id extends StringColumn(this) with PartitionKey[String]
  object name extends StringColumn(this) with PrimaryKey[String]
  object privatekey extends StringColumn(this)
  object publickey extends StringColumn(this)
  object json_claz extends StringColumn(this)
  object created_at extends DateTimeColumn(this)

  override def fromRow(r: Row): SshKeysResult = {
    SshKeysResult(
      id(r),
      org_id(r),
      name(r),
      privatekey(r),
      publickey(r),
      json_claz(r),
      created_at(r))
  }
}


abstract class ConcreteOrg extends SshKeysT with ScyllaConnector {

  override lazy val tableName = "sshkeys"

  def insertNewRecord(sk: SshKeysResult): ResultSet = {
    val res = insert.value(_.id, sk.id)
      .value(_.org_id, sk.org_id)
      .value(_.name, sk.name)
      .value(_.privatekey, sk.privatekey)
      .value(_.publickey, sk.publickey)
      .value(_.json_claz, sk.json_claz)
      .value(_.created_at, sk.created_at)
      .future()
    Await.result(res, 5.seconds)
  }


  def listRecords(org_id: String): ValidationNel[Throwable, Seq[SshKeysResult]] = {
    val resp = select.allowFiltering().where(_.org_id eqs org_id).fetch()
    (Await.result(resp, 5.seconds)).successNel
  }
  def getRecord(id: String): ValidationNel[Throwable, Option[SshKeysResult]] = {
    val res = select.allowFiltering().where(_.name eqs id).one()
    Await.result(res, 5.seconds).successNel
  }
}


object SshKeys extends ConcreteOrg with ImplicitJsonFormats {

  private def sshNel(input: String): ValidationNel[Throwable, SshKeysInput] = {
    (Validation.fromTryCatchThrowable[SshKeysInput, Throwable] {
      parse(input).extract[SshKeysInput]
    } leftMap { t: Throwable => new MalformedBodyError(input, t.getMessage) }).toValidationNel
  }

  private def SshKeysSet(id: String, org_id: String, c: SshKeysInput): ValidationNel[Throwable, SshKeysResult] = {
    (Validation.fromTryCatchThrowable[SshKeysResult, Throwable] {
      SshKeysResult(id, org_id, c.name, c.privatekey, c.publickey, "Megam::SshKey", DateHelper.now())
    } leftMap { t: Throwable => new MalformedBodyError(c.json, t.getMessage) }).toValidationNel
  }

  def create(authBag: Option[io.megam.auth.stack.AuthBag], input: String): ValidationNel[Throwable, SshKeysResult] = {
    for {
      c <- sshNel(input)
      uir <- (UID("SSH").get leftMap { u: NonEmptyList[Throwable] => u })
      sk <- SshKeysSet(uir.get._1 + uir.get._2, authBag.get.org_id, c)
    } yield {
      insertNewRecord(sk)
      sk
    }
  }

  def findByOrgId(authBag: Option[io.megam.auth.stack.AuthBag]): ValidationNel[Throwable, Seq[SshKeysResult]] = {
    (listRecords(authBag.get.org_id) leftMap { t: NonEmptyList[Throwable] =>
      new ResourceItemNotFound(authBag.get.email, "Sshkeys = nothing found.")
    }).toValidationNel.flatMap { nm: Seq[SshKeysResult] =>
        Validation.success[Throwable, Seq[SshKeysResult]](nm).toValidationNel
    }
 }

 def findByName(sshKeysNameList: Option[List[String]]): ValidationNel[Throwable, SshKeysResults] = {
   (sshKeysNameList map {
     _.map { sshKeysName =>
       (getRecord(sshKeysName) leftMap { t: NonEmptyList[Throwable] =>
         new ServiceUnavailableError(sshKeysName, (t.list.map(m => m.getMessage)).mkString("\n"))
       }).toValidationNel.flatMap { xso: Option[SshKeysResult] =>
         xso match {
           case Some(xs) => {
             Validation.success[Throwable, SshKeysResults](List(xs.some)).toValidationNel //screwy kishore, every element in a list ?
           }
           case None => {
             Validation.failure[Throwable, SshKeysResults](new ResourceItemNotFound(sshKeysName, "")).toValidationNel
           }
         }
       }
     }
   } map {
     _.foldRight((SshKeysResults.empty).successNel[Throwable])(_ +++ _)
   }).head
 }

}
