// Comment to get more information during initialization
logLevel := Level.Info

// Typesafe snapshots
resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"


resolvers += "Sonatype Snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype Releases"  at "https://oss.sonatype.org/content/repositories/releases"


 resolvers += "JBoss repository" at "https://repository.jboss.org/nexus/content/repositories/"    
 

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.1.1")
