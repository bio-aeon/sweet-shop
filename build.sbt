import Dependencies._

organization in ThisBuild := "su.wps"
version in ThisBuild := "0.1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.10"

val commonScalacOptions = Seq(
  "-deprecation",
  "-feature",
  "-encoding",
  "UTF-8",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-language:postfixOps",
  "-language:higherKinds",
  "-language:existentials",
  "-language:reflectiveCalls",
  "-Ypartial-unification"
)

lazy val root =
  (project in file("."))
    .settings(name := "sweet-shop")
    .aggregate(authApi, authImpl, webGateway)

lazy val authApi = project in file("microservices/auth-api")

lazy val authImpl = (project in file("microservices/auth-impl"))
  .settings(
    scalacOptions ++= commonScalacOptions,
    libraryDependencies ++= Seq(
      monix,
      http4sBlazeServer,
      http4sCirce,
      http4sDsl,
      doobieCore,
      doobiePostgres,
      doobieHikari,
      pureConfig,
      plivo
    )
  )
  .dependsOn(authApi)

lazy val webGateway = (project in file("microservices/web-gateway"))
  .settings(
    scalacOptions ++= commonScalacOptions,
    libraryDependencies ++= Seq(http4sBlazeServer, http4sCirce, http4sDsl, monix)
  )
