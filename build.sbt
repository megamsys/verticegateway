import sbt._
import Process._
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.archetypes.ServerLoader
import NativePackagerHelper._
import NativePackagerKeys._

import com.typesafe.sbt.packager.archetypes.ServerLoader.{SystemV, Upstart}

import play.Play.autoImport._
import PlayKeys._

scalaVersion := "2.10.4"

scalacOptions := Seq(
	"-target:jvm-1.7",
	"-deprecation",
	"-feature",
 	"-optimise",
  	"-Xcheckinit",
  	"-Xlint",
  	"-Xverify",
 // 	"-Yconst-opt",  	available in scala 2.11
  	"-Yinline",
  	"-Ywarn-all",
  	"-Yclosure-elim",
  	"-language:postfixOps",
  	"-language:implicitConversions",
  	"-Ydead-code")

incOptions := incOptions.value.withNameHashing(true)

packageArchetype.java_server

com.typesafe.sbt.packager.debian.Keys.version in Debian <<= (com.typesafe.sbt.packager.debian.Keys.version, sbt.Keys.version) apply { (v, sv) =>
      val nums = (v split "[^\\d]")
      "%s" format (sv)
    }

maintainer in Linux := "Rajthilak <rajthilak@megam.co.in>"

packageSummary in Linux := "REST based API server - Gateway for Megam."

packageDescription in Linux := "REST based API server which acts as the Gateway server for Megam platform. The API server protects the resources using HMAC based authorization, as provided to a customer during onboarding."

daemonUser in Linux := "megam" // user which will execute the application

daemonGroup in Linux := "megam"    // group which will execute the application

debianPackageDependencies in Debian ++= Seq("curl (>= 7.29)", "python-thrift (>= 0.8.0)", "openjdk-7-jre-headless (>= 7u51)", "bash (>= 4.2)")

debianPackageRecommends in Debian += "riak"

serverLoading in Debian := Upstart

rpmVendor := "Megam Systems"

mappings in Universal ++= directory("bin")
