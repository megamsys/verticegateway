package models.admin.audits

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import models.tosca.KeyValueList
import net.liftweb.json._
import io.megam.util.Time
import org.joda.time.{DateTime, Period}
import org.joda.time.format.DateTimeFormat
import models.admin.ReportResult


object Scavenger {
  def apply(email: String): Scavenger = new Scavenger(email)
}

class Scavenger(email: String) {

  def myorgs = models.team.Organizations.findByEmail(email)

  def nukeLittered: ValidationNel[Throwable, Option[String]] = {
    for {
      aal <- littered
    } yield aal
  }

  def nukeDeployed: ValidationNel[Throwable, Option[String]] = {
    for {
      orgs <-   myorgs leftMap { err: NonEmptyList[Throwable] ⇒ err }
      aal  <-   deployed(orgs)
    } yield aal
  }

  def nukeTelemetry = {
    for {
      hd  <- models.billing.Billedhistories.delete(email)
      bhd <- models.billing.Billingtransactions.delete(email)
      bad <- models.billing.Balances.delete(email)
      chd <- models.billing.Credits.delete(email)
      qud <- models.billing.Quotas.delete(email)
      qud <- models.tosca.Sensors.delete(email)
    } yield "telemetry.done".some
  }

  def nukeWhitePebbles = {
    for {
      hd  <- models.events.EventsBilling.delete(email)
      bhd <- models.events.EventsContainer.delete(email)
      bad <- models.events.EventsVm.delete(email)
    } yield "whitepebbles.done".some
  }

  def nukeIdentity: ValidationNel[Throwable, Option[io.megam.auth.stack.AccountResult]] = {
    for {
      aal <-   delete
    } yield aal
  }

  private def littered = {
   for {
     snps <- models.snapshots.Snapshots.deleteByEmail(email)
     diks <- models.disks.Disks.deleteByEmail(email)
   } yield  "littered.done".some
  }

  private def deployed(orgs: Seq[models.team.OrganizationsResult]) = {
      (orgs.map { org =>  {
        for {
         asm  <-  models.tosca.Assembly.softDeleteByOrgId(org.id)
         asm  <-  models.tosca.Assembly.hardDeleteByOrgId(org.id)
         comp <-  models.tosca.Component.deleteByOrgId(org.id)
         asms <-  models.tosca.Assemblies.deleteByOrgId(org.id)
         ssh  <-  models.base.SshKeys.delete(org.id)
         dod  <- models.team.Domains.delete(org.id)
       } yield "deployed.done".some
      }
    }).head
  }

  private def delete = {
    for {
      add <- models.addons.Addons.delete(email)
      ord <- models.team.Organizations.delete(email)
      acd <- models.base.Accounts.delete(email)
  } yield acd
 }
}
