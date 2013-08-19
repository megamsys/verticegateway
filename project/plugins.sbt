// Comment to get more information during initialization
logLevel := Level.Info

// Typesafe snapshots

resolvers += "Sonatype Snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

resolvers += "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype Releases"  at "https://oss.sonatype.org/content/repositories/releases"

 resolvers += "JBoss repository" at "https://repository.jboss.org/nexus/content/repositories/"    
 
 resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.1.3")

//Packaging currently only works with native-package 0.5.4 and sbt-release 0.6 
//when we move to sbt 0.13/play 2.2 we need to test it back. 
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.5.4") 

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-s3" % "0.5")

