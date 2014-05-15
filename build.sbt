import sbt._
import Process._
import com.typesafe.sbt.packager.debian.Keys._
import com.typesafe.sbt.packager.linux.LinuxPackageMapping
import S3._

s3Settings

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

com.typesafe.sbt.packager.debian.Keys.name in Debian := "megamplay"

com.typesafe.sbt.packager.debian.Keys.version in Debian <<= (com.typesafe.sbt.packager.debian.Keys.version, sbt.Keys.version) apply { (v, sv) =>
      val nums = (v split "[^\\d]")
      "%s" format (sv)
    }
    
maintainer in Debian:= "Rajthilak <rajthilak@megam.co.in>"

packageSummary := "Cloud Gateway for Megam." 

packageDescription in Debian:= "(REST based) Cloud Gateway server for Megam platform. The API server protects the resources using HMAC based authorization, as provided to a customer during onboarding."

debianPackageDependencies in Debian ++= Seq("curl", "bash (>= 2.05a-11)")

debianPackageRecommends in Debian += "riak"

mappings in upload := Seq((new java.io.File(("%s-%s.deb") format("target/megamplay", "0.5.0")),"0.5/debs/megamplay.deb"))

host in upload := "megampub.s3.amazonaws.com"

credentials += Credentials(Path.userHome / "software" / "aws" / "keys" / "sbt_s3_keys")

S3.progress in S3.upload := true
