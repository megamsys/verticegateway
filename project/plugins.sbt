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
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.7.2-RC1")
