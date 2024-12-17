ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.15"

lazy val root = (project in file("."))
  .settings(
    name := "tapir-vertx-server-zio-bug",
    libraryDependencies ++= Seq(
        "com.softwaremill.sttp.tapir" %% "tapir-vertx-server-zio" % "1.11.10",
        "io.netty" % "netty-all" % "4.1.115.Final",
    )
  )




