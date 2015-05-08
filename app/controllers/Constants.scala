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
package controllers

import java.nio.charset.Charset
import play.api.Logger
import scala.util.{ Try }
import play.api.http.HeaderNames._
import controllers.stack.HeaderConstants._
import java.io._

/**
 * @author ram
 *
 */

case class MegamAdmin(email: String, api_key: String, authority: String)

object Constants {

  val UTF8Charset = Charset.forName("UTF-8")
  val JSON_CLAZ = "json_claz"

  lazy val WithGzipHeader: Map[String, String] = Map(CONTENT_TYPE -> application_gzip)

  lazy val WithGzipHoleHeader: Map[String, String] = WithGzipHeader + (X_Megam_OTTAI -> X_Megam_OTTAI)

  /**
   * The MEGAM_HOME variable is setup during the installation of megamgateway in MEGAM_HOME/.megam_auth
   */
  val MEGAM_HOME = sys.env.get("MEGAM_HOME")

  val MEGAM_FIRST_NAME = "Megam Tour"
  val MEGAM_LAST_NAME  = "Call us"
  val MEGAM_PHONE = "18006186813"
  val SAMPLE_PASSWORD = "$2a$10$ebE.KJITo19bkJ/s8gMFpuXkMh2Tu5vL4eVcgJN7THYD1/YiBNWP2"
  val MEGAM_PASSWORD_RESET_KEY = "nil"
  val MEGAM_PASSWORD_RESET_SENT_AT = "nil"

  val MEGAM_ADMIN_AUTHORITY = "admin"
  val MEGAM_NORMAL_AUTHORITY = "normal"

  val DEFAULT_ORG_NAME = "org.megam"
  val DEFAULT_DOMAIN_NAME = "megam.co"

  

  /*Look for a file /var/lib/megam/.megam_auth with fields
  megam@mypaas.io:<randomlygenerated pw>
  if it doesn't exists then use the defaults
  megam@mypaas.io, IamAtlas{74}NobodyCanSeeME#07*/
  private lazy val adminAuth: MegamAdmin = (for {
    home <- MEGAM_HOME
    auth_file <- Some(home + "/.megam_auth")
    res <- Try(scala.io.Source.fromFile(auth_file).mkString).toOption
    if (res.indexOf(":") > 0)
  } yield {
    val res1 = res.split(":").take(2)
    MegamAdmin(res1(0), res1(1), MEGAM_ADMIN_AUTHORITY)
  }).getOrElse(MegamAdmin("megam@mypaas.io", "IamAtlas{74}NobodyCanSeeME#07", MEGAM_ADMIN_AUTHORITY))

  val MEGAM_ADMIN_EMAIL = adminAuth.email
  val MEGAM_ADMIN_APIKEY = adminAuth.api_key

  val DEMO_EMAIL = "tour@megam.io"
  val DEMO_APIKEY = "faketour"


  val DELETE_REQUEST = "DELETE"

  val MEGAM_PRIMED_DIR = (for {home <- MEGAM_HOME}
 yield { home + File.separator + "megamgateway" }).getOrElse("megamgateway")

 val MEGAM_PRIMED_FILE = (for {home <- MEGAM_HOME}
 yield { home + File.separator + "megamgateway" + File.separator + ".megam_primed"}).getOrElse(".megam_primed")






}
