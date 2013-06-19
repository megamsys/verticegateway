import sbt._
import Process._
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.debian.Keys._
import com.typesafe.sbt.packager.linux.LinuxPackageMapping
import sbtrelease._
import ReleasePlugin._
import ReleaseKeys._

seq(packagerSettings:_*)

maintainer in Debian:= "Rajthilak <rajthilak@rajthilak>"

packageSummary := "API server (REST based) for the megam platform."

packageDescription in Debian:= "API server (REST based) for the megam platform."

com.typesafe.sbt.packager.debian.Keys.name in Debian := "megamplay"


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


 
com.typesafe.sbt.packager.debian.Keys.version in Debian <<= (com.typesafe.sbt.packager.debian.Keys.version, sbtVersion) apply { (v, sv) =>
  sv + "-build-" + (v split "\\." map (_.toInt) dropWhile (_ == 0) map ("%02d" format _) mkString "")
}

debianPackageDependencies in Debian ++= Seq("curl", "java2-runtime", "bash (>= 2.05a-11)")

debianPackageRecommends in Debian += "git"

linuxPackageMappings in Debian <+= (com.typesafe.sbt.packager.debian.Keys.sourceDirectory) map { bd =>
  (packageMapping(
    (bd / "debian/changelog") -> "/usr/share/doc/megam_play/changelog.gz"
  ) withUser "root" withGroup "root" withPerms "0644" gzipped) asDocs()
}