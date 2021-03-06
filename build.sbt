import sbt.url

This / organization := "com.newmotion"
This / scalaVersion := tnm.ScalaVersion.prev

This / description := "MDC logging for http4s/zio"
This / licenses    := Seq("MIT License" -> url("https://github.com/kolov/zop-mdc-logging/blob/master/LICENSE"))

lazy val core = (project in file("core"))
  .settings(
    name               := "http4s-zio-mdc-logging-core",
    crossScalaVersions := Seq(tnm.ScalaVersion.prev, tnm.ScalaVersion.curr),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-interop-cats"       % "2.0.0.0-RC10",
      "org.http4s" %% "http4s-blaze-server" % "0.21.0-M6",
      "org.typelevel" %% "cats-free"        % "2.1.1",
      "org.log4s" %% "log4s"                % "1.8.2"
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
  )
  .enablePlugins(LibPlugin)

lazy val demo = (project in file("demo"))
  .dependsOn(core)
  .settings(
    name         := "http4s-zio-mdc-logging-demo",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % "0.21.0-M6",
      "ch.qos.logback"             % "logback-classic" % "1.2.3"
    ),
    publishArtifact    := false,
    crossScalaVersions := Nil,
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
  )

lazy val root = (project in file("."))
  .aggregate(demo, core)
  .settings(
    publishArtifact    := false,
    crossScalaVersions := Nil
  )
  .enablePlugins(BasicPlugin)
