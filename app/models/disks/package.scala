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
  //  def apply(m: DisksResult): DisksResults = DisksResults(m.some)
    def empty: DisksResults = List() //nel(emptyNR.head, emptyNR.tail)
  }
}

