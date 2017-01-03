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

object Assembly {

  //An Admin can see the assembly of an user
  def show(id: String): ValidationNel[Throwable, AssemblyResults] = models.tosca.Assembly.findById(List(id).some)

}
