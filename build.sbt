import Dependencies._

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val commonSettings = Seq(
  scalacOptions ++= "-deprecation" :: "-feature" :: "-Xlint" :: Nil,
  scalacOptions in (Compile, console) ~= {_.filterNot(_ == "-Xlint")},
  scalafmtOnCompile := true
)

lazy val root = (project in file("."))
  .settings(
    name := "neko-server",
    commonSettings,
    libraryDependencies += scalaTest % Test,
    libraryDependencies += scalaParser,
  )

lazy val chat = (project in file("chat"))
  .settings(
    name := "neko-server-chat",
    commonSettings,
    libraryDependencies += scalaTest % Test,
    libraryDependencies += mysql,
  )
  .dependsOn(root)

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
