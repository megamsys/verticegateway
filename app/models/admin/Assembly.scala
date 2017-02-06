package models.admin

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import db._
import io.megam.auth.funnel.FunnelErrors._
import controllers.Constants._

import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import models.tosca._
import models.admin.audits.Constants


object Assembly {

  //An Admin can see the assembly of an user
  def show(id: String): ValidationNel[Throwable, AssemblyResults] = models.tosca.Assembly.findById(List(id).some)

  //An Admin can trash an assembly/its children/parent
  def delete(asbs_id: String, id: String, email: String): ValidationNel[Throwable, AssemblyResults] = {
    for {
      nls <- models.admin.audits.Trasher(asbs_id,id,email).nukeLittered
      nds <- models.admin.audits.Trasher(asbs_id,id,email).nukeDeployed
      //nts <- models.admin.audits.Trasher(asbs_id,id,email).nukeTelemetry
      //nws <- models.admin.audits.Trasher(asbs_id,id,email).nukeWhitePebbles
    } yield {
      models.admin.audits.AuditLog.createFrom(id,
            models.admin.audits.AuditLogInput(id,  Constants.LOG_ASSEMBLY_DELETE,
                                KeyValueList(Map(
                                    Constants.KIND   -> Constants.DESTROYED_ASSEMBLY,
                                    Constants.STATUS -> Constants.SUCCESS
                                  ))
                                )
                              )
     List[Option[AssemblyResult]](none)
    }
  }

}
