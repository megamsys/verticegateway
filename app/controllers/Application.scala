package controllers


import app.Hellow
import io.megam.auth.funnel._
import io.megam.auth.funnel.FunnelErrors._
import play.api.mvc._

/**
 * @author rajthilak
 *
 */
//class Application @Inject() (cache: CacheApi) extends Controller with APIAuthElement {
object Application extends Controller {

  //Shows index page. with all the status.
  def index = Action { implicit request =>
    Ok(Hellow.buccaneer.json)
  }

}
