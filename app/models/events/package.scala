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
package object events {

  type EventsVmResults = List[Option[EventsVmResult]]

  object EventsVmResults {
    val emptyNR = List(Option.empty[EventsVmResult])
    def apply(m: Option[EventsVmResult]) = List(m)
    def apply(m: EventsVmResult): EventsVmResults = EventsVmResults(m.some)
    def empty: EventsVmResults = List() //nel(emptyNR.head, emptyNR.tail)
  }
}
