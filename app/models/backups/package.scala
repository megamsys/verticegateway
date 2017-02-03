package models

import scalaz._
import Scalaz._
import scalaz.NonEmptyList
import scalaz.NonEmptyList._

import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import models.Constants._

/**
 * @author ranjitha
 *
 */
package object backups {

  type BackupsResults = List[Option[BackupsResult]]

  object BackupsResults {
    val emptyNR = List(Option.empty[BackupsResult])
    def apply(m: Option[BackupsResult]) = List(m)
    def apply(m: BackupsResult): BackupsResults = BackupsResults(m.some)
    def empty: BackupsResults = List() //nel(emptyNR.head, emptyNR.tail)
  }
}
