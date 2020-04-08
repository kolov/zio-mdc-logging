import sbt.url

This / description := "Oauth2 authentication and user session middleware for http4s"
This / licenses := Seq("MIT License" -> url("https://github.com/kolov/zop-mdc-logging/blob/master/LICENSE"))
This / scalaVersion := "2.13.1"


lazy val core = (project in file("core")).settings(
  name := "http4s-zio-mdc-logging-core",
  scalaVersion := "2.13.1",
  libraryDependencies ++= Seq(
    "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC10",
    "org.http4s" %% "http4s-blaze-server" % "0.21.0-M6",
    "org.typelevel" %% "cats-free" % "2.1.1",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  ),
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
)
  .enablePlugins(JavaAppPackaging)

lazy val demo = (project in file("demo"))
  .dependsOn(core)
  .settings(
    name := "http4s-zio-mdc-logging-demo",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % "0.21.0-M6",
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
  )


lazy val root = (project in file("."))
  .aggregate(demo, core)

 