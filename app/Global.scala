/*
** Copyright [2013-2014] [Megam Systems]
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
import play.api.http.Status._
import play.api.http.HeaderNames._
import play.api.mvc._
import play.api.mvc.Results._
import play.filters.gzip.{GzipFilter}
import scala.concurrent.Future

/**
 * We do bunch of things in Global, a gzip response is sent back to the client when the
 * header has "Content-length" > 5000
 object Global extends WithFilters(new GzipFilter(shouldGzip = (request, response) =>
  response.headers.get(CONTENT_LENGTH).exists(_.toInt > 5000))) with GlobalSettings {
*/

object Global with GlobalSettings {

  override def onStart(app: Application) {
    play.api.Logger.info("megamgateway - started")
  }

  override def onStop(app: Application) {
    play.api.Logger.info("megamgateway - going down.")
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[play.api.mvc.Result] = {
    Future.successful(InternalServerError(
      views.html.errorPage(ex)))
  }

  override def onHandlerNotFound(request: RequestHeader): Future[play.api.mvc.Result] = {
    Future.successful(NotFound(
      views.html.errorPage(new Throwable(NOT_FOUND + ":" + request.path + " NOT_FOUND"))))
  }
}
