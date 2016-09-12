package controllers

import app.MConfig
import play.api.http.HeaderNames._
import io.megam.auth.stack.HeaderConstants._

/**
 * @author ram
 *
 */

object Constants {

  val VERSION = "1.5.1"

  lazy val WithGzipHeader: Map[String, String] = Map(CONTENT_TYPE -> application_gzip)

  lazy val WithGzipHoleHeader1: Map[String, String] = WithGzipHeader + (X_Megam_OTTAI -> X_Megam_OTTAI)

  lazy val WithGzipHoleHeader: Map[String, String] = WithGzipHeader + (X_Megam_PUTTUSAVI -> X_Megam_PUTTUSAVI)

  val MEGAM_HOME = sys.env.get("MEGAM_HOME").getOrElse("/var/lib/megam") //THIS

  val TEST_EMAIL = "test@megam.io"
  val TEST_APIKEY = "IamAtlas{74}NobdyCanSedfefdeME#07"
  val TEST_PASSWORD = "$2a$10$ebE.KJITo19bkJ/s8gMFpuXkMh2Tu5vL4eVcgJN7THYD1/zjcmxq3"
  val MEGAM_TEST_FIRST_NAME = "Megam Test"

  val DEMO_EMAIL = "tour@megam.io"
  val DEMO_APIKEY = "faketour"
  val MEGAM_FIRST_NAME = "Vertis Tour"
  val MEGAM_LAST_NAME = "Call us"
  val MEGAM_PHONE = "18006186813"
  val MEGAM_PHONE_VERIFIED = "verified"
  val MEGAM_ACTIVE = "active"
  val MEGAM_BLOCKED = "blocked"
  val MEGAM_APPROVED = "approved"
  val MEGAM_APPROVED_BY_ID ="nil"
  val MEGAM_APPROVED_AT = "nil"
  val MEGAM_LAST_POSTED_AT ="nil"
  val MEGAM_LAST_EMAILED_AT = "nil"
  val MEGAM_PREVIOUS_VISIT_AT = "nil"
  val MEGAM_FIRST_SEEN_AT = "nil"
  val MEGAM_SUSPENDED = "suspend"
  val MEGAM_SUSPENDED_AT = "nil"
  val MEGAM_SUSPENDED_TILL = "nil"
  val MEGAM_REGISTRATION_IP_ADDRESS = "nil"
  val SAMPLE_PASSWORD = "$2a$10$ebE.KJITo19bkJ/s8gMFpuXkMh2Tu5vL4eVcgJN7THYD1/YiBNWP2"
  val MEGAM_PASSWORD_RESET_KEY = "nil"
  val MEGAM_PASSWORD_RESET_SENT_AT = "nil"

  val MEGAM_ADMIN_AUTHORITY = "admin"
  val MEGAM_NORMAL_AUTHORITY = "normal"
}
