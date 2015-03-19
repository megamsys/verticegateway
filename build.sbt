import sbt._
import Process._
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.archetypes.ServerLoader
import NativePackagerHelper._
import NativePackagerKeys._

import com.typesafe.sbt.packager.archetypes.ServerLoader.{SystemV, Upstart,Systemd}

scalaVersion := "2.10.4"

scalacOptions := Seq(
  "-target:jvm-1.7",
  "-deprecation",
  "-feature",
  "-optimise",
  "-Xcheckinit",
  "-Xlint",
  "-Xverify",
  "-Yinline",
  "-Yclosure-elim",
  //"-Yconst-opt",
  //"-Ybackend:GenBCode",
  //"closurify:delegating",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:reflectiveCalls",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-Ydead-code")


incOptions := incOptions.value.withNameHashing(true)

name := "megamgateway"

defaultLinuxInstallLocation := "/usr/share/megam/"

defaultLinuxLogsLocation := "/var/log/megam"

com.typesafe.sbt.packager.debian.Keys.version in Debian <<= (com.typesafe.sbt.packager.debian.Keys.version, sbt.Keys.version) apply { (v, sv) =>
      val nums = (v split "[^\\d]")
      "%s" format (sv)
    }

maintainer in Linux := "Rajthilak <rajthilak@megam.co.in>"

packageSummary in Linux := "REST based API server - Gateway for Megam."

packageDescription in Linux := "REST based API server which acts as the Gateway server for Megam platform. The API server protects the resources using HMAC based authorization, as provided to a customer during onboarding."

daemonUser in Linux := "megam" // user which will execute the application

daemonGroup in Linux := "megam"    // group which will execute the application

debianPackageDependencies in Debian ++= Seq("curl", "megamcommon", "megamsnowflake", "apg", "bash")

debianPackageRecommends in Debian += "riak"

serverLoading in Debian := Upstart

rpmVendor := "Megam Systems"

linuxPackageMappings <+= (normalizedName, daemonUser in Linux, daemonGroup in Linux) map { (name, user, group) =>
      packageTemplateMapping("/var/run/megam/" + name)() withUser user withGroup group withPerms "755"
}


name in Docker := "megamgateway"

maintainer in Docker := "Rajthilak <rajthilak@megam.co.in>"

version in Docker <<= sbt.Keys.version

dockerBaseImage := "dockerfile/java"

dockerRepository := Some("gomegam")

dockerExposedPorts in Docker := Seq(9000, 9443)

dockerExposedVolumes in Docker := Seq("/opt/docker/logs")

rpmRequirements ++= Seq("curl", "megamcommon", "megamsnowflake", "pwgen","java-1.7.0-openjdk-headless", "bash")

rpmLicense := Some("Apache")

serverLoading in Rpm := Systemd
