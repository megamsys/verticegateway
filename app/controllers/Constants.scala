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

  val MEGAM_ADMIN_AUTHORITY = "admin"
  
  val MEGAM_NORMAL_AUTHORITY = "normal"
}
