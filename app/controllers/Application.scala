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


import app.Hellow
import controllers.funnel.FunnelResponse
import controllers.funnel.FunnelErrors._
import play.api.mvc._

/**
 * @author rajthilak
 *
 */
//class Application @Inject() (cache: CacheApi) extends Controller with APIAuthElement {
object Application extends Controller with controllers.stack.APIAuthElement {

  //Shows index page. with all the status.
  def index = Action { implicit request =>
    Ok(Hellow.buccaneer.json)
  }

  def iluvapis = Action { implicit request =>
    Ok(views.html.apiPage())
  }

  //does a list of the events.
  def avastye = Action { implicit request =>
    Ok(views.html.eventsPage(Hellow.events))
  }
}
