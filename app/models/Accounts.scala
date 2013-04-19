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
package models

//import org.mindrot.jbcrypt.BCrypt
import scalikejdbc._
import scalikejdbc.SQLInterpolation._
import models._
/**
 * @author ram
 *
 */
case class Account(id: Int, email: String, password: String, name: String, permission: Permission)

object Accounts {

  val * = { rs: WrappedResultSet => 
    Account(
      id         = rs.int("id"),
      email      = rs.string("email"),
      password   = rs.string("password"),
      name       = rs.string("name"),
      permission = Permission.valueOf(rs.string("permission"))
    )
  }

  /*def authenticate(email: String, password: String): Option[Account] = {
    println("Authenticate model")
    //findByEmail(email).filter { account => BCrypt.checkpw(password, account.password) }
    findByEmail(email).filter { account => true }
  }*/
  def authenticate(email: String, password: String): Boolean = {
    println("Authenticate model")
    //findByEmail(email).filter { account => BCrypt.checkpw(password, account.password) }
    findByEmail(email)//.filter { account => true }
  }

  
  def findByEmail(email: String): Boolean = {
    println("email check")
    DB localTx { implicit s =>
      sql"SELECT * FROM account WHERE email = ${email}".map(*).single.apply()
    }
  }

  def findById(id: Int): Option[Account] = {
    println("id check")
    DB localTx { implicit s =>
      sql"SELECT * FROM account WHERE id = ${id}".map(*).single.apply()
    }
  }

  def findAll: Seq[Account] = {
    DB localTx { implicit s =>
      sql"SELECT * FROM account".map(*).list.apply()
    }
  }

  def create(account: Account) {
    DB localTx { implicit s =>
      import account._
    //  val pass = BCrypt.hashpw(account.password, BCrypt.gensalt())
      val pass = "howdy"
      sql"INSERT INTO account VALUES (${id}, ${email}, ${pass}, ${name}, ${permission.toString})".update.apply()
    }
  }
  
/* 
 * import play.api.mvc.Action
import play.api.Logger
import play.api.mvc.RequestHeader
import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.mvc.Result
import controllers.Application._
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import org.apache.commons.codec.binary.Base64
import play.api.mvc.RawBuffer
import play.api.mvc.Codec
 
/**
 * Obejct contains security actions that can be applied to a specific action called from
 * a controller.
 */
object SecurityActions {
 
  val HMAC_HEADER = "hmac"
  val CONTENT_TYPE_HEADER = "content-type"
  val DATE_HEADER = "Date"
 
  val MD5 = "MD5"
  val HMACSHA1 = "HmacSHA1"
 
  /**
   * Function authenticated is defined as a function that takes as parameter
   * a function. This function takes as argumens a user and a request. The authenticated
   * function itself, returns a result.
   *
   * This Authenticated function will extract information from the request and calculate
   * an HMAC value.
   *
   *
   */
  def Authenticated(f: (User, Request[Any]) => Result) = {
    // we parse this as tolerant text, since our content type
    // is application/vnd.geo.comment+json, which isn't picked
    // up by the default body parsers. Alternative would be
    // to parse the RawBuffer manually
    Action(parse.tolerantText) {
 
      request =>
        {
          // get the header we're working with
          val sendHmac = request.headers.get(HMAC_HEADER);
 
          // Check whether we've recevied an hmac header
          sendHmac match {
 
            // if we've got a value that looks like our header 
            case Some(x) if x.contains(":") && x.split(":").length == 2 => {
 
              // first part is username, second part is hash
              val headerParts = x.split(":");
              val userInfo = User.find(headerParts(0))
 
              // Retrieve all the headers we're going to use, we parse the complete 
              // content-type header, since our client also does this
              val input = List(
                request.method,
                calculateMD5(request.body),
                request.headers.get(CONTENT_TYPE_HEADER),
                request.headers.get(DATE_HEADER),
                request.path)
 
              // create the string that we'll have to sign
              val toSign = input.map(
                a => {
                  a match {
                    case None => ""
                    case a: Option[Any] => a.asInstanceOf[Option[Any]].get
                    case _ => a
                  }
                }).mkString("\n")
 
              // use the input to calculate the hmac
              val calculatedHMAC = calculateHMAC(userInfo.secret, toSign)
 
              // if the supplied value and the received values are equal
              // return the response from the delegate action, else return
              // unauthorized
              if (calculatedHMAC == headerParts(1)) {
                f(userinfo, request)
              } else {
                 Unauthorized
              }
            }
 
            // All the other possibilities return to 401 
            case _ => Unauthorized
          }
        }
    }
  }
 
  /**
   * Calculate the MD5 hash for the specified content
   */
  private def calculateMD5(content: String): String = {
    val digest = MessageDigest.getInstance(MD5)
    digest.update(content.getBytes())
    new String(Base64.encodeBase64(digest.digest()))
  }
 
  /**
   * Calculate the HMAC for the specified data and the supplied secret
   */
  private def calculateHMAC(secret: String, toEncode: String): String = {
    val signingKey = new SecretKeySpec(secret.getBytes(), HMACSHA1)
    val mac = Mac.getInstance(HMACSHA1)
    mac.init(signingKey)
    val rawHmac = mac.doFinal(toEncode.getBytes())
    new String(Base64.encodeBase64(rawHmac))
  }
}
 * 
 */

}