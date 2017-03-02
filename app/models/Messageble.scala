package models

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import scalaz.NonEmptyList._

import cache._
import db._
import models.json.tosca._
import models.Constants._
import io.megam.auth.funnel.FunnelErrors._
import app.MConfig
import models.base._

import io.megam.util.Time
import io.megam.common.uid.UID
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

//for our 1.0 rewrite of gwy.
trait Messageble {

  /**
   * Return the option wrapped queuename to publish
   * @return the {{{Option[String]}}} the Option[string] name of the queue
   */
  def topic(x: Unit): Option[String]

  /**
   * Return the payload as a Map
   * @return the {{{String}}} the published response
   */
  def messages(): String


}
