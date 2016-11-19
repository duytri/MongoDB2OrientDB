import sbt._
import Keys._
import sbtassembly.AssemblyPlugin.autoImport._

name := "MongoDB2OrientDB"
version := "1.0-SNAPSHOT"
organization := "uit.islab"
scalaVersion := "2.12.0"


libraryDependencies ++= Seq(
  "com.orientechnologies" % "orientdb-graphdb" % "2.2.12" % "provided",
  "com.orientechnologies" % "orientdb-core" % "2.2.12" % "provided",
  "com.googlecode.concurrentlinkedhashmap" % "concurrentlinkedhashmap-lru" % "1.4.1" % "provided",
  "com.tinkerpop.blueprints" % "blueprints-core" % "2.6.0" % "provided",
  "com.google.code.gson" % "gson" % "2.8.0",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1"
)

unmanagedBase <<= baseDirectory { base => base / "libs" }

assemblyJarName in assembly := name.value+"-"+version.value+".jar"
mainClass in assembly := Some("main.scala.OrientDbScalaExample")

Keys.fork := true

javaOptions in run ++= Seq(
  "-XX:MaxDirectMemorySize=3826m"
)
