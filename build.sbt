import sbt._
import Process._
import com.typesafe.sbt.packager.archetypes.ServerLoader

name := "verticegateway"

version := "1.0"

scalaVersion := "2.11.7"

organization := "Megam Systems"

homepage := Some(url("https://www.megam.io"))

description := """Vertice Gateway : Scalable RESTful API server for megam vertice
                  in a functional way, built using Riak, Memcache
                  try: https://console.megam.io
                  web: https://www.megam.io"""


javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

initialize := {
  val _ = initialize.value
  if (sys.props("java.specification.version") != "1.8")
    sys.error("Java 8 is required for this project.")
}

javaOptions ++= Seq("-Dconfig.file=" + {
  val home  = System getenv "MEGAM_HOME"
  if (home == null || home.length <=0) sys.error("Must define MEGAM_HOME")
  val gwconfPath = Path(home)
  val gwconf = gwconfPath / "verticegateway" /  "gateway.conf"
  gwconf.toString
},
"-Dlogger.file=" + {
  val home  = System getenv "MEGAM_HOME"
  if (home == null || home.length <=0) sys.error("Must define MEGAM_HOME")
  val logconfPath = Path(home)
  val logconf = logconfPath / "verticegateway" /  "logger.xml"
  logconf.toString
})

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

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"
resolvers += "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"
resolvers += "Spray repo" at "http://repo.spray.io"
resolvers += "Spy Repository" at "http://files.couchbase.com/maven2"
resolvers += "Bintray megamsys" at "https://dl.bintray.com/megamsys/scala/"
resolvers += "Websudos" at "https://dl.bintray.com/websudos/oss-releases/"


val phantomV = "1.16.0"

libraryDependencies ++= Seq(filters, cache,
  "org.yaml" % "snakeyaml" % "1.16",
  "io.megam" %% "libcommon" % "0.37",
  "io.megam" %% "newman" % "1.3.12",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.1",
  "com.websudos"      %%  "phantom-dsl"               % phantomV,
  "com.websudos"      %%  "phantom-testkit"           % phantomV,
  "com.websudos"      %%  "phantom-connectors"        % phantomV,
  "org.specs2" %% "specs2-core" % "3.7-scalaz-7.1.6" % "test",
  "org.specs2" % "specs2-matcher-extra_2.11" % "3.7-scalaz-7.1.6" % "test")

//routesGenerator := InjectedRoutesGenerator

enablePlugins(DebianPlugin)

enablePlugins(RpmPlugin)

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

debianPackageDependencies in Debian ++= Seq("curl", "verticecommon", "verticesnowflake")

debianPackageRecommends in Debian += "riak"

linuxPackageMappings <+= (normalizedName, daemonUser in Linux, daemonGroup in Linux) map { (name, user, group) =>
      packageTemplateMapping("/var/run/megam/" + name)() withUser user withGroup group withPerms "755"
}

rpmVendor := "megam"

rpmUrl := Some("https://www.megam.io")

rpmLicense := Some("Apache v2")

packageArchitecture in Rpm := "x86_64"

serverLoading in Rpm := ServerLoader.Systemd

rpmPost := None // disables starting the server on install

linuxStartScriptTemplate in Rpm := (baseDirectory.value / "src" / "rpm" / "start").asURL
