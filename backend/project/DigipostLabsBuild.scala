import com.typesafe.sbt.SbtNativePackager._
import sbt._
import Keys._
import spray.revolver.RevolverPlugin._

object DigipostLabsBuild extends Build {
  val Organization = "no.digipost"
  val Name = "digipost-labs"
  val Version = "1.1-SNAPSHOT"
  val ScalaVersion = "2.11.2"
  val ScalatraVersion = "2.3.0"
  val Json4SVersion = "3.2.10"
  val JettyVersion = "9.2.1.v20140609"
  val LogbackVersion = "1.0.13"

  lazy val project = Project (
    "digipost-labs",
    file("."),
    settings = Defaults.defaultSettings ++ Revolver.settings ++ packageArchetype.java_application ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      scalacOptions ++= Seq("-deprecation"),
      resolvers += Classpaths.typesafeReleases,
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-json" % ScalatraVersion,
        "org.json4s"   %% "json4s-jackson" % Json4SVersion,
        "org.json4s"   %% "json4s-ext" % Json4SVersion,
        "org.json4s"   %% "json4s-mongo" % Json4SVersion exclude("org.mongodb", "mongo-java-driver"),
        "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
        "org.mongodb"  %% "casbah" % "2.7.3",
        "org.pegdown"  % "pegdown" % "1.4.1",
        "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
        "ch.qos.logback" % "logback-classic" % LogbackVersion,
        "org.slf4j" % "jcl-over-slf4j" % "1.7.5",
        "org.eclipse.jetty" % "jetty-webapp" % JettyVersion,
        "org.eclipse.jetty" % "jetty-nosql" % JettyVersion exclude("org.mongodb", "mongo-java-driver"),
        "org.eclipse.jetty" %  "jetty-servlet" % JettyVersion % "provided;test",
        "org.openid4java" % "openid4java" % "0.9.7",
        "com.typesafe" % "config" % "1.2.1"
      ),
      mainClass in Compile := Some("no.digipost.labs.LabsMain")
    )
  )
}
