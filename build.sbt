import sbt._
import Keys._

libraryDependencies in ThisBuild ++= Seq(
  "com.typesafe.akka" % "akka-stream-experimental_2.10" % "1.0-M2" withSources()
)

lazy val soo = project.in(file(".")).aggregate(core, app)

lazy val core = project

lazy val app = project.dependsOn(core)
