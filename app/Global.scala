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
/**
 * @author ram
 *
 */

import play.api._
import play.api.http.Status._
import play.api.http.HeaderNames._
import play.api.mvc._
import play.api.mvc.Results._
import play.filters.gzip.{ GzipFilter }
import controllers.stack.HeaderConstants._
import scala.concurrent.Future
import controllers._
import java.io._

/**
 * We do bunch of things in Global, a gzip response is sent back to the client when the
 * header has "Content-length" > 5000bytes
 */

object Global extends WithFilters(new GzipFilter(shouldGzip = (request, response) =>
  response.headers.get(CONTENT_TYPE).exists(_.startsWith(application_gzip)))) with GlobalSettings {

  override def onStart(app: Application) {
/* website link for banner text - http://patorjk.com/software/taag/#p=display&f=ANSI%20Shadow&t=Megam%20gateway */
    play.api.Logger.info("""
        /^\       ██████╗  █████╗ ████████╗███████╗██╗    ██╗ █████╗ ██╗   ██╗
        |#|      ██╔════╝ ██╔══██╗╚══██╔══╝██╔════╝██║    ██║██╔══██╗╚██╗ ██╔╝
       |===|     ██║  ███╗███████║   ██║   █████╗  ██║ █╗ ██║███████║ ╚████╔╝
        |0|      ██║   ██║██╔══██║   ██║   ██╔══╝  ██║███╗██║██╔══██║  ╚██╔╝
        | |      ╚██████╔╝██║  ██║   ██║   ███████╗╚███╔███╔╝██║  ██║   ██║
       =====      ╚═════╝ ╚═╝  ╚═╝   ╚═╝   ╚══════╝ ╚══╝╚══╝ ╚═╝  ╚═╝   ╚═╝
    """)
    play.api.Logger.info("started ...")

    val megamprimeddir = new File(Constants.MEGAM_PRIMED_DIR)
    megamprimeddir.mkdirs()

    val megamprimedfile = new File(Constants.MEGAM_PRIMED_FILE)

    megamprimedfile.exists() match {
      case true => play.api.Logger.info(">> Found megamprimed file, skip priming.")
      case false =>
        play.api.Logger.info(">> priming: performing priming.")
        models.PlatformAppPrimer.acc_prep
        play.api.Logger.info(">> priming: account..")
        models.PlatformAppPrimer.mkp_prep
        play.api.Logger.info(">> priming: marketplace..")
        models.PlatformAppPrimer.org_prep
        play.api.Logger.info(">> priming: org..")
        models.PlatformAppPrimer.dmn_prep
        play.api.Logger.info(">> priming: complete.")
        megamprimedfile.createNewFile();
    }
  }

  override def onStop(app: Application) {
    play.api.Logger.info("""
███████╗███████╗███████╗    ██╗   ██╗ █████╗
██╔════╝██╔════╝██╔════╝    ╚██╗ ██╔╝██╔══██╗
███████╗█████╗  █████╗       ╚████╔╝ ███████║
╚════██║██╔══╝  ██╔══╝        ╚██╔╝  ██╔══██║
███████║███████╗███████╗       ██║   ██║  ██║
╚══════╝╚══════╝╚══════╝       ╚═╝   ╚═╝  ╚═╝

     """)
    play.api.Logger.info("going down...")
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
