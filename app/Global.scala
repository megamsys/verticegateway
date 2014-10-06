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
import play.filters.gzip.{ GzipFilter }
import controllers.stack.HeaderConstants._
import scala.concurrent.Future
import controllers._
import java.io._
import Constants._

/**
 * We do bunch of things in Global, a gzip response is sent back to the client when the
 * header has "Content-length" > 5000bytes
 */




object Global extends WithFilters(new GzipFilter(shouldGzip = (request, response) => 
   response.headers.get(CONTENT_TYPE).exists(_.startsWith(application_gzip)))) with GlobalSettings {
  
  

  override def onStart(app: Application) {
    play.api.Logger.info("megamgateway - started---------------------------------------------------------")
  
 /* 
   var DName: String = "~/var/lib/megam/"
   val dir: File = new File(DName);
   dir.mkdir();
  
    
  var FileName: String =  DName + File.separator +  ".megam_primed" 
    val FileObj: File = new File(FileName)	
    */
    
    FileObj.exists() match {
      case true => play.api.Logger.info("ALREADY INITIALIZED")
      case false =>  play.api.Logger.info("DEFAULT INITIALIZATION ARE DONE")
                      models.PlatformAppPrimer.acc_prep
                      models.PlatformAppPrimer.cts_prep
                      models.PlatformAppPrimer.mkp_prep
                      models.PlatformAppPrimer.org_prep
                      models.PlatformAppPrimer.dmn_prep
                      FileObj.createNewFile();
    }
    
    
   /* if (FileObj.exists())
    {
      play.api.Logger.info("DEFAULT SETTING ARE INITIALIZED ALREADY")
    }
    else
    {
      play.api.Logger.info("INITIALIZATION ARE DONE")
    FileObj.createNewFile();
    models.PlatformAppPrimer.acc_prep
    models.PlatformAppPrimer.cts_prep
    models.PlatformAppPrimer.mkp_prep
    models.PlatformAppPrimer.org_prep
    models.PlatformAppPrimer.dmn_prep
    
    
    }
    */
    
    
    play.api.Logger.info("-------------------------------------------------------------------------------")

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
