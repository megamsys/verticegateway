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

import scalaz._
import Scalaz._
import play.api._
import play.api.mvc._
import models._
import controllers.stack.{ APIAuthElement, SourceElement }
import controllers.stack._

/**
 * @author ram
 *
 */
object Logs extends Controller with APIAuthElement with NodesHelper {

  def list = StackAction(parse.tolerantText) { implicit request =>
    val input = (request.body).toString()
    val sentHmacHeader = request.headers.get(HMAC_HEADER);
    val id = getAccountID(sentHmacHeader)
    val nodesJson = models.Nodes.getNodes(id) match {
      case Success(v) => {
        v
      }
      case Failure(_) => ""
    }
    println(nodesJson)
    Ok("" + nodesJson)
    //Redirect("http://localhost:7000/streams/syslog")
  }

  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    Redirect("http://localhost:7000/streams/" + id)
  }

}