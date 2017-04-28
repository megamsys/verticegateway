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

import models.team.OrganizationsResult
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import io.megam.auth.stack.Role.{ADMIN}
import models.tosca.KeyValueList
import models.admin.audits.Constants

object Organizations {

  //An Admin can list all the users
  def list: ValidationNel[Throwable, Seq[OrganizationsResult]] = models.team.Organizations.list

}
