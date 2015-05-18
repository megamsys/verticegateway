/*
  ** Copyright [2013-2015] [Megam Systems]
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package controllers

import controllers.stack._
import controllers.funnel.FunnelErrors._
import controllers.funnel._
import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }
import models._
import play.api._
import play.api.mvc._
import scalaz._
import Scalaz._
import scalaz.Validation._
import play.api.data._
import play.api.data.Form._
import play.api.cache._
import play.api.mvc._
/**
 * @author rajthilak
 *
 */
//class Application @Inject() (cache: CacheApi) extends Controller with APIAuthElement {
object  Application  extends Controller with APIAuthElement {

  //Shows welcome page.
  def index = Action { implicit request =>
    Ok(views.html.index("Megam CMP. Lets kick the tyres."))
  }

   /**
   * POST : Authenticate, verifies if the auth setup is OK.
   * Output: FunnelResponse as JSON with the msg.
   * Accessed via API
   */
  def authenticate = StackAction(parse.tolerantText) { implicit request =>
    val resp = FunnelResponse(apiAccessed.getOrElse("Something strange. Authentication successful, but sans success message. Contact support"), "Megam::Auth").toJson(true)
    Ok(resp)
  }

}
