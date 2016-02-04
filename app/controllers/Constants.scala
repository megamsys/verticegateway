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

import app.MConfig
import play.api.http.HeaderNames._
import controllers.stack.HeaderConstants._

/**
 * @author ram
 *
 */

object Constants {

  val VERSION = "0.9"

  val UTF8Charset = java.nio.charset.Charset.forName("UTF-8")
  val JSON_CLAZ = "json_claz"

  lazy val WithGzipHeader: Map[String, String] = Map(CONTENT_TYPE -> application_gzip)

  lazy val WithGzipHoleHeader1: Map[String, String] = WithGzipHeader + (X_Megam_OTTAI -> X_Megam_OTTAI)

  lazy val WithGzipHoleHeader: Map[String, String] = WithGzipHeader + (X_Megam_PUTTUSAVI -> X_Megam_PUTTUSAVI)

  val MEGAM_HOME = sys.env.get("MEGAM_HOME").getOrElse("/var/lib/megam")

  val TEST_EMAIL = "test@megam.io"
  val TEST_APIKEY = "faketest"
  val TEST_PASSWORD = "$2a$10$ebE.KJITo19bkJ/s8gMFpuXkMh2Tu5vL4eVcgJN7THYD1/zjcmxq3"
  val MEGAM_TEST_FIRST_NAME = "Megam Test"

  val DEMO_EMAIL = "tour@megam.io"
  val DEMO_APIKEY = "faketour"
  val MEGAM_FIRST_NAME = "Megam Tour"
  val MEGAM_LAST_NAME = "Call us"
  val MEGAM_PHONE = "18006186813"
  val SAMPLE_PASSWORD = "$2a$10$ebE.KJITo19bkJ/s8gMFpuXkMh2Tu5vL4eVcgJN7THYD1/YiBNWP2"
  val MEGAM_PASSWORD_RESET_KEY = "nil"
  val MEGAM_PASSWORD_RESET_SENT_AT = "nil"

  val MEGAM_ADMIN_AUTHORITY = "admin"
  val MEGAM_NORMAL_AUTHORITY = "normal"

  val DEFAULT_ORG_NAME = "org.megam"
  val OJA_MKT_YAML = MConfig.marketplaces_yaml
  val OJA_YONPI_DIR = MEGAM_HOME + java.io.File.separator + "verticegateway" + java.io.File.separator + "yonpis"

  val STATE = "state"
  val CREATE = "create"
  val DELETE = "destroy"

  val CONTROL = "control"
  val START = "start"
  val STOP = "stop"


  val BIND = "bind"
  val BUILD = "build"
  val REBOOT = "restart"
  val OPERTATIONS = "operations"
  val UPGRADE = "upgrade"

  val CATTYPE_DOCKER = "microservices"
  val CATTYPE_TORPEDO = "torpedo"
  val DOMAIN = "domain"

 val USERNAME = "username"
 val PASSWORD = "password"
 val SPARKJOBSERVER_INPUT = "SPARKJOBSERVER_INPUT"
 val JAR_NAME = "meglytics"
}
