package controllers

import scalaz._
import Scalaz._
import scalaz.Validation._
import net.liftweb.json._


import io.megam.auth.funnel._
import io.megam.auth.funnel.FunnelErrors._
import play.api.mvc._
import controllers.stack.Results


/**
 * @author rajthilak
 *
 */
object MarketPlaces extends Controller with controllers.stack.APIAuthElement {

  /**
   * GET: findbyEmail: List all the market place names per email
   * Email grabbed from header.
   * Output: JSON (MarketPlacesResult)
   */
  def list = StackAction(parse.tolerantText) { implicit request =>
    play.api.Logger.info("[+] Fetching all the MarketPlaceItems")

    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Invalid header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          models.base.MarketPlaces.listAll match {
            case Success(succ) => {
              implicit val formats = DefaultFormats
              Ok(Results.resultset(models.Constants.MARKETPLACECOLLECTIONCLAZ, compactRender(Extraction.decompose(succ))))
            }
            case Failure(err) =>
              val rn: FunnelResponse = new HttpReturningError(err)
              Status(rn.code)(rn.toJson(true))
          }
        }
        case Failure(err) => {
          val rn: FunnelResponse = new HttpReturningError(err)
          Status(rn.code)(rn.toJson(true))
        }
      }
    }).fold(succ = { a: Result => a }, fail = { t: Throwable => Status(BAD_REQUEST)(t.getMessage) })
  }

  def show(id: String) = StackAction(parse.tolerantText) { implicit request =>
    (Validation.fromTryCatchThrowable[Result, Throwable] {
      reqFunneled match {
        case Success(succ) => {
          val freq = succ.getOrElse(throw new Error("Invalid header."))
          val email = freq.maybeEmail.getOrElse(throw new Error("Email not found (or) invalid."))
          models.base.MarketPlaces.findByFlavor(List(id).some) match {
            case Success(succ) =>
            implicit val formats = DefaultFormats
            Ok(Results.resultset(models.Constants.MARKETPLACECOLLECTIONCLAZ, compactRender(Extraction.decompose(succ))))
            case Failure(err) =>
              val rn: FunnelResponse = new HttpReturningError(err)
              Status(rn.code)(rn.toJson(true))
          }
        }
        case Failure(err) => {
          val rn: FunnelResponse = new HttpReturningError(err)
          Status(rn.code)(rn.toJson(true))
        }
      }
    }).fold(succ = { a: Result => a }, fail = { t: Throwable => Status(BAD_REQUEST)(t.getMessage) })
  }
}
