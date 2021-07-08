import sbt._

object Dependencies {
  object Versions {
    val logback = "1.2.3"
    val cats = "2.6.1"
    val http4s = "0.21.24"
    val monix = "3.0.0"
    val circe = "0.13.0"
    val pureconfig = "0.14.0"
    val newtype = "0.4.4"
    val doobie = "0.13.4"
    val plivo = "4.4.1"
    val jwt = "5.0.0"
    val tofu = "0.10.2"
    val log4Cats = "1.1.1"
    val mouse = "1.0.3"
    val specs2 = "4.12.1"
  }

  val logbackClassic = "ch.qos.logback" % "logback-classic" % Versions.logback
  val catsCore = "org.typelevel" %% "cats-core" % Versions.cats
  val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % Versions.http4s
  val http4sCirce = "org.http4s" %% "http4s-circe" % Versions.http4s
  val http4sDsl = "org.http4s" %% "http4s-dsl" % Versions.http4s
  val monix = "io.monix" %% "monix" % Versions.monix
  val doobieCore = "org.tpolecat" %% "doobie-core" % Versions.doobie
  val doobiePostgres = "org.tpolecat" %% "doobie-postgres" % Versions.doobie
  val doobieHikari = "org.tpolecat" %% "doobie-hikari" % Versions.doobie
  val pureConfig = "com.github.pureconfig" %% "pureconfig" % Versions.pureconfig
  val pureconfigCatsEffect = "com.github.pureconfig" %% "pureconfig-cats-effect" % Versions.pureconfig
  val newtype = "io.estatico" %% "newtype" % Versions.newtype
  val plivo = "com.plivo" % "plivo-java" % Versions.plivo
  val circeGeneric = "io.circe" %% "circe-generic" % Versions.circe
  val jwtCirce = "com.pauldijou" %% "jwt-circe" % Versions.jwt
  val tofuCore = "tf.tofu" %% "tofu-core" % Versions.tofu
  val tofuEnv = "tf.tofu" %% "tofu-env" % Versions.tofu
  val tofuLogging = "tf.tofu" %% "tofu-logging" % Versions.tofu
  val tofuDoobie = "tf.tofu" %% "tofu-doobie" % Versions.tofu
  val log4catsCore = "io.chrisdavenport" %% "log4cats-core" % Versions.log4Cats
  val log4catsSlf4j = "io.chrisdavenport" %% "log4cats-slf4j" % Versions.log4Cats
  val mouse = "org.typelevel" %% "mouse" % Versions.mouse
  val specs2Core = "org.specs2" %% "specs2-core" % Versions.specs2
}
