package controllers.stack

import play.api.mvc.{ Result, Controller }
import scalikejdbc.{DB, DBSession}

import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }
import models._
import play.api._
import play.api.mvc._
import controllers.stack._

trait HMACElement extends StackableController {
  self: Controller =>

  case object HMACSessionKey extends RequestAttributeKey[(DB, DBSession)]

  override def proceed[A](req: RequestWithAttributes[A])(f: RequestWithAttributes[A] => Result): Result = {
        println("HMAC entry  "+req )   
        
        //if (validateToken(req)) super.proceed(req)(f) else BadRequest
        if(SecurityActions.Authenticated(req)) super.proceed(req)(f) else BadRequest
    }
      
 
   /*private def validateToken[A](req: RequestWithAttributes[A]): Boolean = {
    val result = SecurityActions.Authenticated{ implicit request => 
            println("Validate entry")
            Accepted 
            /**
             * Store the HMAC session. 
             * verify if the request gets passed over implicitly
             */            
    }
    /**
     * This is just to make sure the code compiles. 
     */
     println("Validate entry1")
    false
   }*/

  /**Authenticated {
    (user, request) => {
    	// convert the supplied json to a comment object
    	val comment = Json.parse(request.body.asInstanceOf[String]).as[Comment]
 
    	// pass the comment object to a service for processing
    	commentService.storeComment(comment)
    	println(Json.toJson(comment))
        Status(201)
      }
  }**/
  
  implicit def hmacSession[A](implicit req: RequestWithAttributes[A]): DBSession = req.get(HMACSessionKey).get._2 // throw
 
}