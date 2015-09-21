// Comment to get more information during initialization
logLevel := Level.Debug

resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.3")

libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api"       % "1.7.12" force(),
    "org.slf4j" % "slf4j-nop"       % "1.7.12" force(),
    "org.slf4j" % "slf4j-jdk14"     % "1.7.12" force(),
    "org.slf4j" % "jcl-over-slf4j"  % "1.7.12" force()
)
