package controllers

import app.MConfig
import play.api.http.HeaderNames._
import io.megam.auth.stack.HeaderConstants._

/**
 * @author ram
 *
 */
object Constants {

  lazy val WithGzipHeader: Map[String, String] = Map(CONTENT_TYPE -> application_gzip)

  lazy val WithGzipHoleHeader1: Map[String, String] = WithGzipHeader + (X_Megam_OTTAI -> X_Megam_OTTAI)

  lazy val WithGzipHoleHeader: Map[String, String] = WithGzipHeader + (X_Megam_PUTTUSAVI -> X_Megam_PUTTUSAVI)

}
