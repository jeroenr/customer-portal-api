import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "is-typing-api"
  val appVersion      = "2-SNAPSHOT"

  val appDependencies = Seq(
    jdbc,
    cache,
    filters,
    "com.typesafe.play" %% "play-slick" % "0.5.0.8" exclude("org.scala-stm", "scala-stm_2.10.0"),
    "mysql" % "mysql-connector-java" % "5.1.18",
    "net.sourceforge.jtds" % "jtds" % "1.3.1",
    "org.webjars" %% "webjars-play" % "2.2.1",
    "org.squeryl" %% "squeryl" % "0.9.5-6",
    "org.mindrot" % "jbcrypt" % "0.3m",
     "commons-codec" % "commons-codec" % "1.9",

    // Swagger
    "net.eamelink" %% "swaggerkit-play2" % "0.2.0-SNAPSHOT" exclude("org.scala-stm", "scala-stm_2.10.0") exclude("play","play_2.10") excludeAll(ExclusionRule(organization = "com.typesafe.play"))
  )

  lazy val json_utils = project

  lazy val es_client = project.dependsOn(json_utils)

  val main = play.Project(appName, appVersion, appDependencies)
    .settings(
    routesImport += "binders._",

    // Resolvers
      resolvers += Resolver.file("Local repo", file(System.getProperty("user.home") + "/.ivy2/local"))(Resolver.ivyStylePatterns),
    resolvers += "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/",
    resolvers += "Lunatech snapshots repository" at "http://artifactory.lunatech.com/artifactory/snapshots-public/",

    scalacOptions ++= Seq(
      "-unchecked", "-deprecation", "-feature",
      "-language:reflectiveCalls",
      "-language:postfixOps,implicitConversions,experimental.macros,dynamics,existentials,higherKinds"
    )

  ).dependsOn(json_utils, es_client).aggregate(json_utils, es_client)


}

