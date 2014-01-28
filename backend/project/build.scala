import com.typesafe.sbt.SbtNativePackager._
import sbt._
import Keys._
import spray.revolver.RevolverPlugin._

object DigipostLabsBuild extends Build {
  val Organization = "no.digipost"
  val Name = "digipost-labs"
  val Version = "1.1-SNAPSHOT"
  val ScalaVersion = "2.10.3"
  val ScalatraVersion = "2.2.2"
  val Json4SVersion = "3.2.5"
  val JettyVersion = "8.1.13.v20130916"
  val LogbackVersion = "1.0.13"

  lazy val project = Project (
    "digipost-labs",
    file("."),
    settings = Defaults.defaultSettings ++ Revolver.settings ++ packageArchetype.java_application ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-json" % ScalatraVersion,
        "org.json4s"   %% "json4s-jackson" % Json4SVersion,
        "org.json4s"   %% "json4s-ext" % Json4SVersion,
        "org.json4s"   %% "json4s-mongo" % Json4SVersion exclude("org.mongodb", "mongo-java-driver"),
        "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
        "org.mongodb"  %% "casbah" % "2.6.4",
        "org.pegdown"  % "pegdown" % "1.4.1",
        "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
        "ch.qos.logback" % "logback-classic" % LogbackVersion,
        "org.slf4j" % "jcl-over-slf4j" % "1.7.5",
        "org.eclipse.jetty" % "jetty-webapp" % JettyVersion,
        "org.eclipse.jetty" % "jetty-nosql" % JettyVersion exclude("org.mongodb", "mongo-java-driver"),
        "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided;test" artifacts Artifact("javax.servlet", "jar", "jar"),
        "org.openid4java" % "openid4java" % "0.9.7"
      ),
      mainClass in Compile := Some("no.digipost.labs.LabsMain")
    )
  )
}
