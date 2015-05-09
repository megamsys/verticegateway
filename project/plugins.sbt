// Comment to get more information during initialization
logLevel := Level.Info

// Typesafe snapshots

resolvers += "Sonatype Snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

resolvers += "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Typesafe snapshots" at "http://repo.typesafe.com/typesafe/snapshots"

resolvers += "Sonatype Releases"  at "https://oss.sonatype.org/content/repositories/releases"

resolvers += "JBoss repository" at "https://repository.jboss.org/nexus/content/repositories/"

resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.9")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.8.0")

libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api"       % "1.7.7" force(),
    "org.slf4j" % "slf4j-nop"       % "1.7.7" force(),
    "org.slf4j" % "slf4j-jdk14"     % "1.7.7" force(),
    "org.slf4j" % "jcl-over-slf4j"  % "1.7.7" force()
)
