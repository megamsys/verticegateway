import sbt._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "megam_play"

  val appVersion = "1.0"

  val organization = "Megam Systems"

  val homepage = Some(url("http://megam.co"))

  val startYear = Some(2013)

  val description = "A  scalable cloud bridge for messaging systems used to instrument cloud instances."

  /**
   *   if you use groupID %% artifactID % revision instead of groupID % artifactID % revision
   *   (the difference is the double %% after the groupID), sbt will add your project’s Scala version
   *   to the artifact name.
   */
 
  val scalazVersion = "7.0.0"
  val scalaCheckVersion = "1.10.1"
  val specs2Version = "1.14"

  val appDependencies = Seq(
    javaCore, javaEbean, jdbc, javaJdbc,
    "org.scalaz" %% "scalaz-core" % scalazVersion,
    "org.scalaz" %% "scalaz-iteratee" % scalazVersion,
    "org.scalaz" %% "scalaz-effect" % scalazVersion,
    "org.scalaz" %% "scalaz-iterv" % scalazVersion,
    "com.stackmob" %% "scaliak" % "0.7.0",
    "com.stackmob" %% "newman" % "0.15.0-SNAPSHOT",
    "jp.t2v" %% "play2.auth" % "0.9",
    "jp.t2v" %% "play2.auth.test" % "0.9" % "test",
    "com.rabbitmq" % "amqp-client" % "3.0.4",
    "com.github.seratch" %% "scalikejdbc" % "1.5.2",
    "com.github.seratch" %% "scalikejdbc-play-plugin" % "1.5.2",
    "com.github.seratch" %% "scalikejdbc-interpolation" % "1.5.2",
    "postgresql" % "postgresql" % "9.1-901.jdbc4",
    "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
    "org.specs2" %% "specs2" % specs2Version % "test",
    "net.liftweb" % "lift-json" % "2.0",
    "com.github.indykish" % "megam_common_2.10" % "0.1.0-SNAPSHOT")
  val main = play.Project(appName, appVersion, appDependencies).settings(       
      sbt.Keys.resolvers += "Sonatype Snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots",
      sbt.Keys.resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
      sbt.Keys.resolvers += "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"
      // Add your own project settings here      
  )
  

}
