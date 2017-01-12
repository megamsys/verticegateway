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

import models.snapshots.SnapshotsResult
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import io.megam.auth.stack.Role.{ADMIN}

object Snapshots {

  def list: ValidationNel[Throwable, Seq[SnapshotsResult]] = models.snapshots.Snapshots.list

}
