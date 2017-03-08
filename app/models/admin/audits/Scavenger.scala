package models.admin.audits

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._
import scalaz.syntax.SemigroupOps

import models.tosca.KeyValueList
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._

import io.megam.util.Time
import org.joda.time.{DateTime, Period}
import org.joda.time.format.DateTimeFormat
import models.admin.ReportResult


object Scavenger {
  def apply(email: String): Scavenger = new Scavenger(email)
}

class Scavenger(email: String) extends controllers.stack.ImplicitJsonFormats {

  private def myorgs = models.team.Organizations.findByEmail(email)

  def nukeLittered: ValidationNel[Throwable, Option[String]] = {
    for {
      aal <- littered
    } yield aal
  }

  def nukeDeployed: ValidationNel[Throwable, Option[String]] = {
    for {
      orgs <-   myorgs leftMap { err: NonEmptyList[Throwable] ⇒ err }
      aal  <-   deployed(orgs)
    } yield   aal

    "nuked".some.successNel[Throwable] //we keep moving to the next step
  }

  def nukeTelemetry = {
    val t = (for {
      hd  <- models.billing.Billedhistories.delete(email)
      bhd <- models.billing.Billingtransactions.delete(email)
      bad <- models.billing.Balances.delete(email)
      chd <- models.billing.Credits.delete(email)
      qud <- models.billing.Quotas.delete(email)
      qud <- models.tosca.Sensors.delete(email)
    } yield  "nuked".some)

    "nuked".some.successNel[Throwable] //we keep moving to the next step

  }

  def nukeWhitePebbles = {
    for {
      hd  <- models.events.EventsBilling.delete(email)
      bhd <- models.events.EventsContainer.delete(email)
      bad <- models.events.EventsVm.delete(email)
      bad <- models.events.EventsStorage.delete(email)
    } yield  "whitepebbles.done".some
  }

  def nukeIdentity: ValidationNel[Throwable, Option[io.megam.auth.stack.AccountResult]] = {
    for {
      aal <-   delete
    } yield  aal
  }

  private def littered = {
   for {
     snps <- models.disks.Snapshots.deleteByEmail(email)
     diks <- models.disks.Disks.deleteByEmail(email)
     bak  <- models.disks.Backups.deleteByEmail(email)
   } yield  "littered.done".some
  }

  private def deployed(orgs: Seq[models.team.OrganizationsResult]) = {
      (orgs.map { org =>  {
        for {
         asms  <- models.tosca.Assemblies.findByEmail(email, org.id)
         amnk  <- mkTrashers(asms)
         aenk  <- invokeTrashers(amnk)
         ssh   <- models.base.SshKeys.delete(org.id)
         dod   <- models.team.Domains.delete(org.id)
       } yield "deployed.done".some
      }
    })

    "nuked.deployed".some.successNel[Throwable] //we keep moving to the next step
  }

    private def delete = {
      for {
        add <- models.addons.Addons.delete(email)
        ord <- models.team.Organizations.delete(email)
        dcd <- models.base.Accounts.delete(email)
      } yield dcd
    }

    private def mkTrashers(ars :Seq[models.tosca.AssembliesResult]) = {
      (ars.map { ar =>
          ar.assemblies.map(models.admin.audits.Trasher(ar.id, _, email))
      }).flatten.successNel
    }

    private def invokeTrashers(trs :Seq[models.admin.audits.Trasher])  = {
      val n = trs.map(_.nukeDeployed)

      if (!n.isEmpty) {  n.head  } else {
        models.tosca.AssemblyResult("","","","", models.tosca.ComponentLinks.empty,"",
          models.tosca.PoliciesList.empty, models.tosca.KeyValueList.empty,
          models.tosca.KeyValueList.empty, "", "", "", utils.DateHelper.now()).successNel
      }
    }

}
