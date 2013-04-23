/* 
** Copyright [2012] [Megam Systems]
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
/**
 * @author ram
 *
 */

import play.api._
import play.api.mvc._
import play.api.mvc.Results._

import models._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
    if (Accounts.findAll.isEmpty) {
      Seq(
        Account(1, "admin@node.com", "admin", "secret3", Administrator),
        Account(2, "bob@example.com", "secret", "Bob", NormalUser),
        Account(3, "chris@example.com", "secret", "Chris", NormalUser)) foreach Accounts.create
    }
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    InternalServerError(
      views.html.errorPage(ex))
  }

}