package models

import scalaz._
import Scalaz._
import scalaz.NonEmptyList
import scalaz.NonEmptyList._
import models.Constants._
import models.billing._
import io.megam.auth.stack.AccountResult
import io.megam.auth.stack.{ Name, Phone, Password, States, Approval, Dates, Suspend }
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.nio.charset.Charset
import controllers.stack.ImplicitJsonFormats
import net.liftweb.json.JsonParser._

/**
 * @author rajthilak
 *
 */
package object billing {
 implicit val formats = DefaultFormats
  type BalancesResults = List[Option[BalancesResult]]

  object BalancesResults {
    val emptyPR = List(Option.empty[BalancesResult])
    def apply(m: BalancesResult): BalancesResults = List(m.some)
    def empty: BalancesResults = List()
  }

  def atAccUpdate(email: String): ValidationNel[Throwable,Option[AccountResult]] = {
    val approval = Approval("true", "", "")
    val  acc = AccountResult("", Name.empty, Phone.empty, email, new String(), Password.empty, States.empty, approval, Suspend.empty, new String(), Dates.empty)
    models.base.Accounts.update(email, compactRender(Extraction.decompose(acc)))
  }

}
