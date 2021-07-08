import Dependencies._

organization in ThisBuild := "su.wps"
version in ThisBuild := "0.1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.13.6"

val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings",
  "-Ymacro-annotations"
)

val commonSettings = Seq(
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.0" cross CrossVersion.patch),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
)

lazy val root =
  (project in file("."))
    .settings(name := "sweet-shop")
    .aggregate(authApi, authImpl, webGateway)

lazy val authApi = project in file("microservices/auth-api")

lazy val authImpl = (project in file("microservices/auth-impl"))
  .settings(
    commonSettings,
    scalacOptions ++= commonScalacOptions,
    libraryDependencies ++= Seq(
      monix,
      http4sBlazeServer,
      http4sCirce,
      http4sDsl,
      doobieCore,
      doobiePostgres,
      doobieHikari,
      circeGeneric,
      pureConfig,
      pureconfigCatsEffect,
      newtype,
      plivo,
      jwtCirce,
      tofuCore,
      tofuEnv,
      tofuLogging,
      tofuDoobie,
      log4catsCore,
      log4catsSlf4j,
      mouse,
      specs2Core % Test
    ),
    resolvers ++= Seq(Resolver.sonatypeRepo("releases"))
  )
  .dependsOn(authApi, errors)

lazy val webGateway = (project in file("microservices/web-gateway"))
  .settings(
    scalacOptions ++= commonScalacOptions,
    libraryDependencies ++= Seq(
      http4sBlazeServer,
      http4sCirce,
      http4sDsl,
      monix,
      circeGeneric
    )
  )
  .dependsOn(authApi, errors)

lazy val errors = (project in file("modules/errors"))
  .settings(scalacOptions ++= commonScalacOptions, libraryDependencies ++= Seq(catsCore))
