import sbt.Keys._
import scala.Some

name := "json-utils"

organization := "net.fromamsterdamwithlove"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.3"

libraryDependencies += "com.github.fge" % "json-schema-validator" % "2.1.8"

libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.2.3"

libraryDependencies += "org.specs2" %% "specs2" % "2.3.7" % "test"

scalacOptions ++= Seq(
  "-unchecked", "-deprecation", "-feature",
  "-language:reflectiveCalls",
  "-language:postfixOps,implicitConversions,experimental.macros,dynamics,existentials,higherKinds"
)

publishTo := Some(Resolver.file("Local", Path.userHome / ".m2" / "repository" asFile))
