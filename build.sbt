import sbt._
import Process._
import com.typesafe.sbt.packager.archetypes.ServerLoader

name := "virtenginegateway"

version := "1.5.5"

scalaVersion := "2.11.8"

organization := "VirtEngine"

homepage := Some(url("https://virtengine.com"))

description := """VirtEngine Gateway : RESTful API gateway for VirtEngine using HMAC authentication
VirtEngine gateway connects to an opensource database ScyllaDB or,
compatible cassandra 3.x. A messaging layer via Nsqd (nsq.io) provides an
extra layer of decoupling from the virtualization or container platforms.
.
VirtEngine extends the benefits of OpenNebula virtualization platform to allow
single click launch of application, high availability using ceph, autoscaling
and billing integrated.
.
This package contains playframework based API server managing cassandra for
VirtEngine."""


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
  val gwconf = gwconfPath / "virtenginegateway" /  "gateway.conf"
  gwconf.toString
},
"-Dlogger.file=" + {
  val home  = System getenv "MEGAM_HOME"
  if (home == null || home.length <=0) sys.error("Must define MEGAM_HOME")
  val logconfPath = Path(home)
  val logconf = logconfPath / "virtenginegateway" /  "logger.xml"
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


val phantomV = "1.25.4"

libraryDependencies ++= Seq(filters, cache,
  "org.yaml"          %  "snakeyaml" % "1.17",
  "io.megam"          %% "libcommon" % "1.9.0",
  "io.megam"          %% "newman" % "1.3.12",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.8",
  "com.websudos"      %% "phantom-dsl"               % phantomV,
  "com.websudos"      %% "phantom-connectors"        % phantomV,
  "org.specs2"        %% "specs2-core" % "3.7.2-scalaz-7.1.7" % "test",
  "org.specs2"        %  "specs2-matcher-extra_2.11" % "3.7.2-scalaz-7.1.7" % "test")

//routesGenerator := InjectedRoutesGenerator

enablePlugins(DebianPlugin)

enablePlugins(RpmPlugin)

NativePackagerKeys.defaultLinuxInstallLocation := "/usr/share/detio/"

NativePackagerKeys.defaultLinuxLogsLocation := "/var/log/detio/"

version in Debian <<= (version, sbt.Keys.version) apply { (v, sv) =>
      val nums = (v split "[^\\d]")
      "%s" format (sv)
}


maintainer in Linux := "VirtEngine Humans <hello@virtengine.com>"

packageSummary in Linux := "VirtengineGateway is a REST API server."

packageDescription in Linux := "REST based API server which acts as the Gateway server for VirtEngine."

daemonUser in Linux := "virtengine" // user which will execute the application

daemonGroup in Linux := "virtengine"    // group which will execute the application

debianPackageDependencies in Debian ++= Seq("curl", "virtenginecommon")

linuxPackageMappings <+= (normalizedName, daemonUser in Linux, daemonGroup in Linux) map { (name, user, group) =>
      packageTemplateMapping("/var/run/detio/" + name)() withUser user withGroup group withPerms "755"
}

rpmVendor := "DET.io"

rpmUrl := Some("https://docs.virtengine.com")

rpmLicense := Some("MIT")

packageArchitecture in Rpm := "x86_64"

serverLoading in Rpm := ServerLoader.Systemd

rpmPost := None // disables starting the server on install

linuxStartScriptTemplate in Rpm := (baseDirectory.value / "src" / "rpm" / "start").asURL
