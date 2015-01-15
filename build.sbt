name := "escalade-hipchat"

version := "0.0.1"

scalaVersion := "2.11.4"

organization := "com.zaneli"

crossScalaVersions := Seq("2.10.4", "2.11.4")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

libraryDependencies ++= {
  Seq(
    "com.github.nscala-time" %% "nscala-time" % "1.6.0" % "compile",
    "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2" % "compile",
    "org.slf4j" % "slf4j-api" % "1.7.9" % "compile",
    "ch.qos.logback" % "logback-classic" % "1.1.2" % "compile",
    "org.json4s" %% "json4s-native" % "3.2.11" % "compile",
    "org.scalaj" %% "scalaj-http" % "1.1.0" % "compile",
    "org.specs2" %% "specs2-core" % "2.4.15" % "test"
  )
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath + "/.m2/repository")))

pomExtra := (
  <url>https://github.com/zaneli/escalade-hipchat</url>
  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:zaneli/escalade-hipchat.git</url>
    <connection>scm:git:git@github.com:zaneli/escalade-hipchat.git</connection>
  </scm>
  <developers>
    <developer>
      <id>zaneli</id>
      <name>Shunsuke Otani</name>
      <url>https://github.com/zaneli</url>
    </developer>
  </developers>
)

