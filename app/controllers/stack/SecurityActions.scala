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
package controllers.stack
import play.api.mvc.Action
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
import models._

import jp.t2v.lab.play2.stackc.{ RequestWithAttributes, RequestAttributeKey, StackableController }
/**
 * @author rajthilak
 *
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
  def Authenticated(f: Request[Any] => Result) = {
    // we parse this as tolerant text, since our content type
    // is application/vnd.geo.comment+json, which isn't picked
    // up by the default body parsers. Alternative would be
    // to parse the RawBuffer manually    	   
    Action {
      implicit request =>
        {
          // get the header we're working with
          val sendHmac = request.headers.get(HMAC_HEADER);

          // Check whether we've recevied an hmac header
          sendHmac match {

            // if we've got a value that looks like our header
            case Some(x) if x.contains(":") && x.split(":").length == 2 => {

              // first part is username, second part is hash
              val headerParts = x.split(":");
              //val userInfo = User.find(headerParts(0))

              // Retrieve all the headers we're going to use, we parse the complete
              // content-type header, since our client also does this
              val input = List(
                request.method,
                //calculateMD5(request.body),
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
              val calculatedHMAC = calculateHMAC("secret1", toSign)
              // if the supplied value and the received values are equal
              // return the response from the delegate action, else return
              // unauthorized
              val authMaybe = Accounts.authenticate(headerParts(0), headerParts(1))
              authMaybe match {
                case Some(account) =>
                  println("authorizied successfully buddy. " + account)
                  f(request)
                case None =>
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
    /*val signingKey = new SecretKeySpec(secret.getBytes(), HMACSHA1)
    val mac = Mac.getInstance(HMACSHA1)
    mac.init(signingKey)
    val rawHmac = mac.doFinal(toEncode.getBytes())
    new String(Base64.encodeBase64(rawHmac))
    * */
    "secret"
  }
}



/**
 * 
 * package dispatch.oauth

import collection.Map
import collection.immutable.{TreeMap, Map=>IMap}

import javax.crypto
import java.net.URI

import org.apache.http.protocol.HTTP.UTF_8
import org.apache.commons.codec.binary.Base64.encodeBase64
import org.apache.http.client.methods.HttpRequestBase

case class Consumer(key: String, secret: String)
case class Token(value: String, secret: String)
object Token {
  def apply(m: Map[String, String]): Token = Token(m("oauth_token"), m("oauth_token_secret"))
}

/** Import this object's methods to add signing operators to dispatch.Request */
object OAuth {
  /** @return oauth parameter map including signature */
  def sign(method: String, url: String, user_params: Map[String, Any], consumer: Consumer, 
      token: Option[Token], verifier: Option[String]) = {
    val oauth_params = IMap(
      "oauth_consumer_key" -> consumer.key,
      "oauth_signature_method" -> "HMAC-SHA1",
      "oauth_timestamp" -> (System.currentTimeMillis / 1000).toString,
      "oauth_nonce" -> System.nanoTime.toString
    ) ++ token.map { "oauth_token" -> _.value } ++ 
      verifier.map { "oauth_verifier" -> _ }
    
    val encoded_ordered_params = (
      new TreeMap[String, String] ++ (user_params ++ oauth_params map %%)
    ) map { case (k, v) => k + "=" + v } mkString "&"
    
    val message = %%(method :: url :: encoded_ordered_params :: Nil)
    
    val SHA1 = "HmacSHA1";
    val key_str = %%(consumer.secret :: (token map { _.secret } getOrElse "") :: Nil)
    val key = new crypto.spec.SecretKeySpec(bytes(key_str), SHA1)
    val sig = {
      val mac = crypto.Mac.getInstance(SHA1)
      mac.init(key)
      new String(encodeBase64(mac.doFinal(bytes(message))))
    }
    oauth_params + ("oauth_signature" -> sig)
  }
  
  //normalize to OAuth percent encoding
  private def %% (str: String): String = (Http % str) replace ("+", "%20") replace ("%7E", "~")
  private def %% (s: Seq[String]): String = s map %% mkString "&"
  private def %% (t: (String, Any)): (String, String) = (%%(t._1), %%(t._2.toString))
  
  private def bytes(str: String) = str.getBytes(UTF_8)
  
  /** Add OAuth operators to dispatch.Request */
  implicit def Request2RequestSigner(r: Request) = new RequestSigner(r)
  
  class RequestSigner(r: Request) {
    
    // sign requests with an Authorization header
    def <@ (consumer: Consumer): Request = sign(consumer, None, None)
    def <@ (consumer: Consumer, token: Token): Request = sign(consumer, Some(token), None)
    def <@ (consumer: Consumer, token: Token, verifier: String): Request = 
      sign(consumer, Some(token), Some(verifier))
    
    /** add token value as a query string parameter, for user authorization redirects */
    def <<? (token: Token) = r <<? IMap("oauth_token" -> token.value)

    /** Sign request by reading Post (<<) and query string parameters */
    private def sign(consumer: Consumer, token: Option[Token], verifier: Option[String]) = r next { req =>
      val oauth_url = Http.to_uri(r.host, req).toString.split('?')(0)
      val query_params = split_decode(req.getURI.getRawQuery)
      val oauth_params = OAuth.sign(req.getMethod, oauth_url, query_params ++ (req match {
        case before: Post => before.values
        case _ => IMap()
      }), consumer, token, verifier )
      req.addHeader("Authorization", "OAuth " + oauth_params.map { 
        case (k, v) => (Http % k) + "=\"%s\"".format(Http % v)
      }.mkString(",") )
      req
    }

    def >% [T] (block: IMap[String, String] => T) = r >- ( split_decode andThen block )
    def as_token = r >% { Token(_) }
    
    val split_decode: (String => IMap[String, String]) = {
      case null => IMap.empty
      case query => IMap.empty ++ query.split('&').map { nvp =>
        ( nvp split "=" map Http.-% ) match { 
          case Seq(name, value) => name -> value
          case Seq(name) => name -> ""
        }
      }
    }
  }
  
}    

 * 
 */
