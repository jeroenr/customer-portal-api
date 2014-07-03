import sbt._
import sbt.Keys._
import scala.Some

name := "es-client"

organization := "net.fromamsterdamwithlove"

// Credentials
//credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

//resolvers += "iBanx 3rd party" at "https://www.ibanx.nl/developer/nexus/content/groups/public"

// TODO: ADD MANAGED DEPENDENCY WHEN SCALASTIC IS IN MAVEN CENTRAL
//libraryDependencies += "org.scalastic" % "scalastic_2.10" % "1.1"


// TODO: REMOVE WHEN SCALASTIC IS IN MAVEN CENTRAL
libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.0.1"

libraryDependencies += "com.google.code.findbugs" % "jsr305" % "1.3.9"

libraryDependencies += "org.elasticsearch" % "elasticsearch" % "1.1.0"
// END OF REMOVAL


libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.0"

libraryDependencies += "commons-io" % "commons-io" % "2.4" % "test"

libraryDependencies += "org.specs2" %% "specs2" % "2.3.7" % "test"

libraryDependencies += "net.fromamsterdamwithlove" %% "json-utils" % "0.1-SNAPSHOT"

scalacOptions ++= Seq(
  "-unchecked", "-deprecation", "-feature",
  "-language:reflectiveCalls",
  "-language:postfixOps,implicitConversions,experimental.macros,dynamics,existentials,higherKinds"
)

publishTo := Some(Resolver.file("Local", Path.userHome / ".m2" / "repository" asFile))

