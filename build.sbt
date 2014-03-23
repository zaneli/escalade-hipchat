name := "escalade-hipchat"

version := "0.0.1"

scalaVersion := "2.10.3"

organization := "com.zaneli"

scalacOptions ++= Seq("-unchecked", "-deprecation")

libraryDependencies ++= {
  Seq(
    "com.typesafe" %% "scalalogging-slf4j" % "1.0.1" % "compile",
    "org.slf4j" % "slf4j-api" % "1.7.5" % "compile",
    "ch.qos.logback" % "logback-classic" % "1.0.13" % "compile",
    "org.scalaj" %% "scalaj-http" % "0.3.11" % "compile",
    "net.liftweb" %% "lift-json" % "2.5.1" % "compile",
    "org.scalaj" % "scalaj-time_2.10.2" % "0.7" % "compile",
    "org.specs2" %% "specs2" % "2.3.4" % "test"
  )
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath + "/.m2/repository")))