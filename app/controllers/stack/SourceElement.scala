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
package controllers.stack

import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }
import models._
import play.api._
import play.api.mvc._
import play.api.mvc.{ Result, Controller }
import com.stackmob.scaliak.ScaliakClient

/**
 * @author rajthilak
 *
 */

/*
 * sub trait for stackable controller,
 * proceed method was override here for our request changes, 
 * And result return in super trait proceed method,
 * when stack action is called then this stackable controller is executed 
 * 
 */
trait SourceElement extends StackableController {

  self: Controller =>

  case object DomainObjectKey extends RequestAttributeKey[ScaliakClient]

  override def proceed[A](req: RequestWithAttributes[A])(f: RequestWithAttributes[A] => Result): Result = {

    /*
    * Domain Objects client creation, 
    * db was connected and req return in super class with domainobjectkey and db 
    * otherwise bad request return
    */
    DomainObjects.clientCreate() match {
      case db => super.proceed(req.set(DomainObjectKey, db))(f)
      case _  => BadRequest
    }
  }

  implicit def domainImplicit[A](implicit req: RequestWithAttributes[A]): ScaliakClient = req.get(DomainObjectKey).get
}