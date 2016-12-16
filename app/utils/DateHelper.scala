
package utils

import scalaz._
import Scalaz._
import org.joda.time.{DateTime, Period}
import org.joda.time.format.DateTimeFormat
import io.megam.util.Time


/**
 * @author rajthilak
 *
 */
object DateHelper {

  def now(created_at: String = null): DateTime = {
    if (created_at == "" || created_at == null) {
      return DateTime.parse(Time.now.toString, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z")).withTimeAtStartOfDay()
    } else {
      return DateTime.parse(created_at, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z"))
    }
  }

  def toTimeRange(created_at: DateTime) = {
    new Tuple2(
      created_at,
      DateTime.parse(Time.now.toString, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z")))
  }

}
