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
/**
 * @author ram
 *
 */

import scalaz._
import Scalaz._
import scalaz.Validation._
import play.api._
import play.api.mvc._
import play.api.mvc.Results._

import org.megam.common.riak.GunnySack

import models._
import scalaz._
import scalaz.Validation._
import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz._

import models._
import controllers.stack.APIAuthElement
import controllers.stack._
import controllers.funnel.FunnelErrors._
import controllers.funnel.{ FunnelResponse, FunnelResponses }

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    play.api.Logger.info("Megam Play %s App - started".format("0.1"))
    (Validation.fromTryCatch[Unit] {
      models.Predefs.create match {
        case Success(succ) =>
          play.api.Logger.debug("""Predefs created successfully.
            |
            |Cache gets loaded upon first fetch. %nLoaded values are ----->%n{%s}""".format(succ.toString))
        case Failure(err) =>
          val rn: FunnelResponses = new HttpReturningError(err)
          play.api.Logger.error(rn)
      }
    })
  }

  override def onStop(app: Application) {
    play.api.Logger.info("Application shutdown...")
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    InternalServerError(
      views.html.errorPage(ex))
  }

  override def onHandlerNotFound(request: RequestHeader): play.api.mvc.Result = {
    NotFound(
      views.html.errorPage(new Throwable(request.path)))
  }
}