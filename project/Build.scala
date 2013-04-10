import sbt._
import Keys._
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
  val appDependencies = Seq(
    javaCore, javaEbean, jdbc, javaJdbc,
    "com.stackmob" %% "scaliak" % "0.4.0",
    "jp.t2v" %% "play2.auth" % "0.9",
    "jp.t2v" %% "play2.auth.test" % "0.9" % "test",
    "com.rabbitmq" % "amqp-client" % "3.0.4",
    "com.github.seratch" %% "scalikejdbc" % "1.5.1",
    "com.github.seratch" %% "scalikejdbc-play-plugin" % "1.5.1",
    "com.github.seratch" %% "scalikejdbc-interpolation" % "1.5.1",
    "postgresql" % "postgresql" % "9.1-901.jdbc4",// your JDBC driver
    "org.scalaz" % "scalaz-core_2.10" % "7.0.0-M9",
    "org.scalaz" % "scalaz-effect_2.10" % "7.0.0-M9") 

  val main = play.Project(appName, appVersion, appDependencies).settings( // Add your own project settings here      
  )

}
