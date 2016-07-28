/**
 * @author ram
 *
 */

import scalaz._
import Scalaz._
import scalaz.effect.IO
import scalaz.EitherT._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import play.api._
import play.api.http.Status._
import play.api.http.HeaderNames._
import play.api.mvc._
import play.api.mvc.Results._
import play.filters.gzip.{ GzipFilter }
import io.megam.auth.stack.HeaderConstants._
import scala.concurrent.Future
import controllers._
import java.io._

/**
 * We do bunch of things in Global, a gzip response is sent back to the client when the
 * header has "Content-length" > 5000bytes
 */

object Global extends WithFilters(new GzipFilter(shouldGzip = (request, response) =>
  response.headers.get(CONTENT_TYPE).exists(_.startsWith(application_gzip)))) with GlobalSettings {

  override def onStart(app: play.api.Application) {
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
/*
 * PlatformAppPrimer - both acc_prep and mkp_prep gets done successfully only if scyllaDB holds the data in settings table
 */
        for {
          m <- utils.PlatformAppPrimer.acc_prep
      //    mkp <- utils.PlatformAppPrimer.mkp_prep
        } yield {
          play.api.Logger.info(">> priming: successful.")
        }
        play.api.Logger.info(">> priming: complete.")
  }

  override def onStop(app: play.api.Application) {
    play.api.Logger.info("""
███████╗███████╗███████╗    ██╗   ██╗ █████╗
██╔════╝██╔════╝██╔════╝    ╚██╗ ██╔╝██╔══██╗
███████╗█████╗  █████╗       ╚████╔╝ ███████║
╚════██║██╔══╝  ██╔══╝        ╚██╔╝  ██╔══██║
███████║███████╗███████╗       ██║   ██║  ██║
╚══════╝╚══════╝╚══════╝       ╚═╝   ╚═╝  ╚═╝

     """)
    play.api.Logger.info("Shark bait...")
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
