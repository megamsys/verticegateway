package models.admin

import scalaz._
import Scalaz._
import scalaz.NonEmptyList
import models.Constants._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

package object reports {

  type Reported = String

  object Reported {
    def apply(m: String): Reported = m
 }

}
