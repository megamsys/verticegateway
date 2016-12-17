
package utils

import scalaz._
import Scalaz._


/**
 * @author rajthilak
 *
 */
object StringStuff {

  def NilOrNot(rip: String, aor: String): String = {
    rip == null || rip == "" match {
      case true => return aor
      case false => return rip
    }
  }

}
