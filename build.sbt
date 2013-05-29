import sbt._
import Process._
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.debian.Keys._
import sbtrelease._
import ReleasePlugin._
import ReleaseKeys._

maintainer := "Rajthilak <git@github.com:rajthilakmca/megam_play.git>"

packageSummary := "Simple Build Tool for Scala-driven builds"

packageDescription := """This script provides a native way to run the Simple Build Tool, a build tool for Scala software, also called SBT."""

seq(packagerSettings:_*)

// DEBIAN SPECIFIC
sbt.Keys.name in Debian := "sbt"

sbt.Keys.version in Debian <<= (sbt.Keys.version, sbtVersion) apply { (v, sv) =>
  sv + "-build-" + (v split "\\." map (_.toInt) dropWhile (_ == 0) map ("%02d" format _) mkString "")
}

debianPackageDependencies in Debian ++= Seq("curl", "java2-runtime", "bash (>= 2.05a-11)")

debianPackageRecommends in Debian += "git"

linuxPackageMappings in Debian <+= (sbt.Keys.sourceDirectory) map { bd =>
  (packageMapping(
    (bd / "debian/changelog") -> "/usr/share/doc/sbt/changelog.gz"
  ) withUser "root" withGroup "root" withPerms "0644" gzipped) asDocs()
}