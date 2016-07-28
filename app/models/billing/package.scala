package models

import scalaz._
import Scalaz._
import scalaz.NonEmptyList
import scalaz.NonEmptyList._
import models.Constants._
import models.billing._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset

/**
 * @author rajthilak
 *
 */
package object billing {

  type BalancesResults = List[Option[BalancesResult]]

  object BalancesResults {
    val emptyPR = List(Option.empty[BalancesResult])
    def apply(m: BalancesResult): BalancesResults = List(m.some)
    def empty: BalancesResults = List()
  }


}
