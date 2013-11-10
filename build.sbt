import sbt._
import Process._
import com.typesafe.sbt.packager.debian.Keys._
import com.typesafe.sbt.packager.linux.LinuxPackageMapping
import S3._

s3Settings

scalaVersion := "2.10.3"

scalacOptions := Seq(
	"-unchecked", 
	"-deprecation",
	"-feature",
 	"-optimise",
  	"-Xcheckinit",
  	"-Xlint",
  	"-Xverify",
  	"-Yinline-warnings",
  	"-Yclosure-elim",
  	"-language:postfixOps",
  	"-language:implicitConversions",
  	"-Ydead-code")

com.typesafe.sbt.packager.debian.Keys.name in Debian := "megamplay"

com.typesafe.sbt.packager.debian.Keys.version in Debian <<= (com.typesafe.sbt.packager.debian.Keys.version, sbt.Keys.version) apply { (v, sv) =>
      val nums = (v split "[^\\d]")
      "%s" format (sv)
    }
    
maintainer in Debian:= "Rajthilak <rajthilak@megam.co.in>"

packageSummary := "Cloud API server - Megam." 

packageDescription in Debian:= " (REST based) Cloud API server for Megam platform.The API server protects the resources using HMAC based authorization, as provided to a customer during onboarding."

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping((bd / "bin/mp") -> "/usr/local/share/megamplay/bin/mp")
   withUser "root" withGroup "root" withPerms "0755")
}

linuxPackageMappings <+= (baseDirectory) map { bd =>
  val src = bd / "target/staged"
  val dest = "/usr/local/share/megamplay/lib"
  LinuxPackageMapping(
    for {
      path <- (src ***).get
      if !path.isDirectory
    } yield path -> path.toString.replaceFirst(src.toString, dest)
  )
}

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping((bd / "conf/application-production.conf") -> "/usr/local/share/megamplay/conf/application-production.conf")
   withConfig())
}

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping((bd / "conf/application-logger.xml") -> "/usr/local/share/megamplay/conf/application-logger.xml")
   withConfig())
}

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping((bd / "conf/routes") -> "/usr/local/share/megamplay/conf/routes")
   withConfig())
}

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping((bd / "conf/messages") -> "/usr/local/share/megamplay/conf/messages")
   withConfig())
}

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping((bd / "conf/play.plugins") -> "/usr/local/share/megamplay/conf/play.plugins")
   withConfig())
}

 
debianPackageDependencies in Debian ++= Seq("curl", "java2-runtime", "bash (>= 2.05a-11)")

debianPackageRecommends in Debian += "riak"

linuxPackageMappings <+= (baseDirectory) map { bd =>
  packageMapping(
    (bd / "copyright") -> "/usr/share/doc/megam_play/copyright"
  ) withPerms "0644" asDocs()
}

linuxPackageMappings in Debian <+= (com.typesafe.sbt.packager.debian.Keys.sourceDirectory) map { bd =>
  (packageMapping(
    (bd / "debian/changelog") -> "/usr/share/doc/megam_play/changelog.gz"
  ) withUser "root" withGroup "root" withPerms "0644" gzipped) asDocs()
}

mappings in upload := Seq((new java.io.File(("%s-%s.deb") format("target/megamplay", "0.1.0")),"debs/megam_play.deb"))

host in upload := "megampub.s3.amazonaws.com"

credentials += Credentials(Path.userHome / "software" / "aws" / "keys" / "sbt_s3_keys")

S3.progress in S3.upload := true