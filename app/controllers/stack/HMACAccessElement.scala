package app.controllers.stack

import play.api.mvc.{ Result, Controller }
import scalikejdbc._
import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }
import models._
import play.api._
import play.api.mvc._
import controllers.stack._
trait HMACAccessElement extends StackableController {
  self: Controller =>

  case object DBSessionKey extends RequestAttributeKey[(DB, DBSession)]

  override def proceed[A](req: RequestWithAttributes[A])(f: RequestWithAttributes[A] => Result): Result = {
    println("Db connect")
    if (validateToken(req)) super.proceed(req)(f)
    else BadRequest
  }
  
  private def validateToken[A](req: RequestWithAttributes[A]): Boolean = {
    //user <- Accounts.authenticate("bob@example", "secret")
   SecurityActions.Authenticated(req)
  }//yield true).getOrElse(false)


 /*private def validateToken[A](req: RequestWithAttributes[A]): Boolean = (for {
    //user <- Accounts.authenticate("bob@example", "secret")
   SecurityActions.Authenticated (req)}) 
  //yield true).getOrElse(false)
*/
  implicit def dbSession[A](implicit req: RequestWithAttributes[A]): DBSession = req.get(DBSessionKey).get._2 // throw

}
  

