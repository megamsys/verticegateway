package controllers.stack

import scalaz._
import Scalaz._
import scalaz.Validation._
import scala.concurrent.Future
import net.liftweb.json._
import net.liftweb.json.JsonParser._


import controllers.Constants._
import io.megam.auth.funnel._
import io.megam.auth.funnel.FunnelErrors._
import io.megam.auth.stack.AccountResult
import play.api.mvc._
import play.api.libs.iteratee.Enumerator
import models.base.Accounts

/**
 * @author rajthilak
 *
 */
object Results {
  protected val JSONClazKey = models.Constants.JSON_CLAZ
  protected val ResultsKey = "results"

 def resultset(jsonclaz: String, result: String): String = {
    val res = JsonParser.parse(result)
    prettyRender(JObject(JField(JSONClazKey, JString(jsonclaz)) :: JField(ResultsKey, res) :: Nil))
  }

}
