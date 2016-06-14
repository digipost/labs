import com.typesafe.sbt.SbtNativePackager._
import sbt._
import Keys._
import spray.revolver.RevolverPlugin._

object DigipostLabsBuild extends Build {
  val Organization = "no.digipost"
  val Name = "digipost-labs"
  val Version = "1.2-SNAPSHOT"

  val ScalaVersion = "2.11.4"
  val ScalatraVersion = "2.3.0"
  val Json4SVersion = "3.2.11"
  val JettyVersion = "9.2.5.v20141112"

  // %% means scala lib compiled for scala major version x.xx
  //  % means java lib or lib not compiled for a specific scala version
  val dependencies = Seq (
    "org.scalatra"            %% "scalatra"           % ScalatraVersion,
    "org.scalatra"            %% "scalatra-json"      % ScalatraVersion,
    "org.scalatra"            %% "scalatra-scalatest" % ScalatraVersion % "test",
    "org.json4s"              %% "json4s-jackson"     % Json4SVersion,
    "org.json4s"              %% "json4s-ext"         % Json4SVersion,
    "org.json4s"              %% "json4s-mongo"       % Json4SVersion exclude("org.mongodb", "mongo-java-driver"),
    "net.databinder.dispatch" %% "dispatch-core"      % "0.11.2",
    "org.mongodb"             %% "casbah"             % "2.7.4",
    "org.pegdown"              % "pegdown"            % "1.4.1",
    "ch.qos.logback"           % "logback-classic"    % "1.1.2",
    "org.slf4j"                % "jcl-over-slf4j"     % "1.7.7",
    "org.eclipse.jetty"        % "jetty-webapp"       % JettyVersion,
    "org.eclipse.jetty"        % "jetty-nosql"        % JettyVersion exclude("org.mongodb", "mongo-java-driver"),
    "org.eclipse.jetty"        % "jetty-servlet"      % JettyVersion % "provided;test",
    "org.openid4java"          % "openid4java"        % "0.9.7",
    "com.typesafe"             % "config"             % "1.2.1"
  )

  lazy val project = Project (
    Name,
    file("."),
    settings = Defaults.defaultSettings ++ Revolver.settings ++ packageArchetype.java_application ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      scalacOptions ++= Seq("-deprecation", "-feature"),
      resolvers += Classpaths.typesafeReleases,
      resolvers += Resolver.mavenLocal,
      libraryDependencies ++= dependencies,
      mainClass in Compile := Some("no.digipost.labs.LabsMain")
    )
  )
}
