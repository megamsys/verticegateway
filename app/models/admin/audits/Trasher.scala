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
import models.tosca.AssemblyResult


object Trasher {
  def apply(asbs_id: String, id: String, email: String): Trasher = new Trasher(asbs_id, id, email)
}

class Trasher(asbs_id: String, id: String ,email: String) {

  private def myorgs = models.team.Organizations.findByEmail(email)

  def nukeLittered: ValidationNel[Throwable, Option[String]] = {
    for {
      aal <- littered
    } yield aal
  }

  def nukeDeployed: ValidationNel[Throwable, Option[AssemblyResult]] = {
    for {
      orgs <-   myorgs leftMap { err: NonEmptyList[Throwable] ⇒ err }
      aal  <-   deployed(orgs)
    } yield aal
  }

  def nukeTelemetry = {
    for {
      hd  <- models.billing.Billedhistories.deleteByAssembly(id, email)
      qud <- models.tosca.Sensors.deleteByAssembly(id, email)
    } yield   "telemetry.done".some
  }

  def nukeWhitePebbles = {
    for {
      hd  <- models.events.EventsBilling.deleteByAssembly(id, email)
      bhd <- models.events.EventsContainer.deleteByAssembly(id, email)
      bad <- models.events.EventsVm.deleteByAssembly(id, email)
    } yield   "whitepebbles.done".some
  }

  private def littered = {
   for {
     snps <- models.disks.Snapshots.deleteByAssembly(id, email)
     diks <- models.disks.Disks.deleteByAssembly(id, email)
     baks <- models.disks.Backups.deleteByAssembly(id, email)
   } yield  "littered.done".some
  }

  private def deployed(orgs: Seq[models.team.OrganizationsResult]) = {
     for {
       asm  <-  models.tosca.Assembly.softDeleteById(id, email)
       ahm  <-  models.tosca.Assembly.hardDeleteById(asm.org_id, id, asm.created_at)
       comp <-  models.tosca.Component.deleteById(asm.org_id, asm.components)
       asms <-  models.tosca.Assemblies.hardDeleteById(asm.org_id, asbs_id)
     } yield ahm
  }
}
