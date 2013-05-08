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
import controllers.stack.HMACElement
import controllers.stack._
import com.stackmob.scaliak.ScaliakClient
import play.api.libs.json.Json
import play.api.libs.json.JsString
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import org.apache.commons.codec.binary.Base64
/**
 * @author ram
 *
 */

/*
 * this controller for HMAC authentication and access riak
 * If HMAC authentication is true then post or list the nodes are executed
 *  
 */
object Nodes extends Controller with HMACElement with SourceElement {

  /*
   * parse.tolerantText to parse the RawBody 
   * get requested body and put into the riak bucket
   */
  def post = StackAction(parse.tolerantText) { implicit request =>
    val input = (request.body).toString()
    models.Nodes.put("accounts", "4", input)
    Ok("Post Action succeeded")
  }

  /*
   * show the message details
   * 
   */
  def show(id: Long) = StackAction { implicit request =>
    val title = "messages detail "
    Ok(views.html.index(title + id))
  }

  /*
   * list the particular Id values
   * 
   */
  def list = StackAction(parse.tolerantText) { implicit request =>
    val result = models.Nodes.findById("accounts", "2")
    result match {
      case Some(node) => {
        Ok("Nodes Page succeeded ========>" + node.key + "   :   " + node.value)
      }
      case None =>
        Ok("Key not Found")
    }
  }

}

