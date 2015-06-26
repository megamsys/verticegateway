import sbt._
import Process._

name := "megamgateway"

version := "0.9"

scalaVersion := "2.11.7"

organization := "Megam Systems"

homepage := Some(url("https://www.megam.io"))

description := """Megam Gateway : RESTful API server for the megam built using
                  Riak, Snowflake(UID), Memcache
                  try: https://console.megam.io
                  web: https://www.megam.io"""


javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

initialize := {
  val _ = initialize.value
  if (sys.props("java.specification.version") != "1.8")
    sys.error("Java 8 is required for this project.")
}

scalacOptions := Seq(
  "-deprecation",
  "-feature",
  "-optimise",
  "-Xcheckinit",
  "-Xlint",
  "-Xverify",
  "-Yinline",
  "-Yclosure-elim",
  "-Yconst-opt",
  "-Ydelambdafy:method" ,
  "-Ybackend:GenBCode",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:reflectiveCalls",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-Ydead-code")


incOptions := incOptions.value.withNameHashing(true)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"
resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases"
resolvers += "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"
resolvers += "Spray repo" at "http://repo.spray.io"
resolvers += "Spy Repository" at "http://files.couchbase.com/maven2"
resolvers += "Bintray megamsys" at "https://dl.bintray.com/megamsys/scala/"
resolvers += "Bintray scalaz" at "https://dl.bintray.com/scalaz/releases/"

libraryDependencies ++= Seq(filters, cache,
  "com.github.mumoshu" %% "play2-memcached-play23" % "0.7.0",
  "jp.t2v" %% "play2-auth" % "0.13.2",
  "org.yaml" % "snakeyaml" % "1.15",
  "io.megam" %% "libcommon" % "0.9",
  "io.megam" %% "newman" % "1.3.10" % "test")

//routesGenerator := InjectedRoutesGenerator

enablePlugins(DebianPlugin)

NativePackagerKeys.defaultLinuxInstallLocation := "/usr/share/megam/"

NativePackagerKeys.defaultLinuxLogsLocation := "/var/log/megam"

version in Debian <<= (version, sbt.Keys.version) apply { (v, sv) =>
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

linuxPackageMappings <+= (normalizedName, daemonUser in Linux, daemonGroup in Linux) map { (name, user, group) =>
      packageTemplateMapping("/var/run/megam/" + name)() withUser user withGroup group withPerms "755"
}
