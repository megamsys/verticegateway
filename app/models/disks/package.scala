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
package object disks {

  type DisksResults = List[Option[DisksResult]]

  object DiskResults {
    val emptyNR = List(Option.empty[DisksResult])
    def apply(m: Option[DisksResult]) = List(m)
    def empty: DisksResults = List()
  }

  type BackupsResults = List[Option[BackupsResult]]

  object BackupsResults {
    val emptyNR = List(Option.empty[BackupsResult])
    def apply(m: Option[BackupsResult]) = List(m)
    def apply(m: BackupsResult): BackupsResults = BackupsResults(m.some)
    def empty: BackupsResults = List() //nel(emptyNR.head, emptyNR.tail)
  }

  type SnapshotsResults = List[Option[SnapshotsResult]]

  object SnapshotsResults {
    val emptyNR = List(Option.empty[SnapshotsResult])
    def apply(m: Option[SnapshotsResult]) = List(m)
    def apply(m: SnapshotsResult): SnapshotsResults = SnapshotsResults(m.some)
    def empty: SnapshotsResults = List() //nel(emptyNR.head, emptyNR.tail)
  }
}
