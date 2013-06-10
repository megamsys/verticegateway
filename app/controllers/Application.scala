/* 
** Copyright [2012-2013] [Megam Systems]
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

import play.api._
import play.api.mvc._
import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }
import models._
import controllers.stack._

/**
 * @author rajthilak
 *
 */
object Application extends Controller with APIAuthElement {

  def index = Action {
    Ok(views.html.index("Your new application is Ready."))
  }

  def authenticate = StackAction(parse.tolerantText) { implicit request =>
    Ok("""Authorization successful for 'email:' api_key matched: '%s'
            |
            |Your email and api_key  combination was verified successully.  Try other API invocation. 
            |Read https://api.megam.co, http://docs.megam.co for more help. Ask for help on the forums.""".format("none:?").stripMargin
      + "\n" + apiAccessed)
  }

}