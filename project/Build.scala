import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._

object ApplicationBuild extends Build {

  val appName = "megamgateway"

  val appVersion = "0.5.0"

  val organization = "Megam Systems"

  val homepage = Some(url("http://www.gomegam.com"))

  val startYear = Some(2014)

  val description = "Megam Gateway :REST API server for the megam platform using Riak, Snowflake(UID), Memcache"

  val play2AuthVersion = "0.12.0"

  val megamVersion = "0.5.4"

  val appDependencies = Seq(
    javaCore, cache, javaEbean, filters,
    "com.stackmob" %% "scaliak" % "0.10.0-SNAPSHOT" from "https://s3-ap-southeast-1.amazonaws.com/megampub/0.5/jars/scaliak_2.10-0.10.0-SNAPSHOT.jar",
    "com.github.indykish" %% "megam_common" % megamVersion,
    "com.github.mumoshu" %% "play2-memcached" % "0.6.0",
    "jp.t2v" %% "play2-auth" % play2AuthVersion,
    "jp.t2v" %% "play2-auth-test" % play2AuthVersion % "test",
    "org.yaml" % "snakeyaml" % "1.13",
    "com.stackmob" %% "newman" % "1.3.5" % "test")

  val root = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
    version := appVersion,
    libraryDependencies ++= appDependencies,
    sbt.Keys.resolvers += "Sonatype Snapshots" at Opts.resolver.sonatypeSnapshots.root,
    sbt.Keys.resolvers += "Sonatype Releases" at Opts.resolver.sonatypeStaging.root,
    sbt.Keys.resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    sbt.Keys.resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases",
    sbt.Keys.resolvers += "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots",
    sbt.Keys.resolvers += "Twitter Repo" at "http://maven.twttr.com", // finagle
    sbt.Keys.resolvers += "Spray repo" at "http://repo.spray.io", //spray client used in newman.
    sbt.Keys.resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.
    )

}
