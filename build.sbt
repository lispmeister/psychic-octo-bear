name := "psychic-octo-bear"

version := "1.0"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-unchecked", "-deprecation")

resolvers ++= Seq(
    "Typesafe repo"      at "http://repo.typesafe.com/typesafe/releases/",
    "sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
    "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases",
    "twitter"            at "http://maven.twttr.com",
    "jclarity"           at "https://repo.jclarity.com/content/groups/public/",
    "clojars"            at "https://clojars.org/repo")

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"


libraryDependencies ++= Seq(
        "com.typesafe.akka"     %% "akka-actor"         % "2.3.2"  % "compile",
        "com.typesafe.akka"     %% "akka-kernel"        % "2.3.2"  % "compile",
        "com.typesafe.akka"     %% "akka-slf4j"         % "2.3.2"  % "compile",
        "com.typesafe.akka"     %% "akka-testkit"       % "2.3.2"  % "compile, test",
        "com.typesafe.akka"     %% "akka-remote"        % "2.3.2"  % "compile, test",
        "org.slf4j"             % "slf4j-api"           % "1.5.2"  % "compile",
        "ch.qos.logback"        % "logback-classic"     % "0.9.29" % "compile", 
        "net.liftweb"           %% "lift-webkit"        % "2.6-M2" % "compile",
        "net.liftweb"           %% "lift-testkit"       % "2.6-M2" % "compile->default",
        "net.liftweb"           %% "lift-mapper"        % "2.6-M2" % "compile",
        "net.liftweb"           %% "lift-json"          % "2.6-M2" % "compile",
        "net.liftweb"           %% "lift-json-ext"      % "2.6-M2" % "compile",
        "net.liftmodules"       %% "lift-jquery-module_2.6"        % "2.5",
        "com.github.levkhomich" %% "akka-tracing-core"  % "0.2" changing(),
        "com.github.nscala-time" %% "nscala-time"       % "0.8.0")
