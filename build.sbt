import NativePackagerKeys._

scalaVersion := "2.10.4"

name := "finagle-nr"

version := "0.0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-http" % "6.24.0"
)


