import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

scalaVersion := "2.10.4"

name := "finagle-nr"

version := "0.0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-http" % "6.24.0",
  "com.newrelic.agent.java" % "newrelic-api" % "3.12.1"
)

packageArchetype.java_application

scalacOptions ++= Seq(
  "-feature"
)

javaOptions ++= Seq(
  "-Xmx384m",
  "-Xss512k",
  "-XX:+UseCompressedOops",
  "-javaagent:libs/newrelic-agent.jar"

)

fork in run := true
