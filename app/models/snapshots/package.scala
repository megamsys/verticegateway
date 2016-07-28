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
package object snapshots {

  type SnapshotsResults = List[Option[SnapshotsResult]]

  object SnapshotsResults {
    val emptyNR = List(Option.empty[SnapshotsResult])
    def apply(m: Option[SnapshotsResult]) = List(m)
    def apply(m: SnapshotsResult): SnapshotsResults = SnapshotsResults(m.some)
    def empty: SnapshotsResults = List() //nel(emptyNR.head, emptyNR.tail)
  }
}
