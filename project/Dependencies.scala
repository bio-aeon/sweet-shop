import sbt._

object Dependencies {
  object Versions {
    val logback = "1.2.3"
    val cats = "2.6.1"
    val catsEffect = "2.5.1"
    val shapeless = "2.3.3"
    val aecor = "0.19.0"
    val http4s = "0.21.24"
    val monix = "3.0.0"
    val bouncyCastle = "1.69"
    val circe = "0.13.0"
    val pureconfig = "0.14.0"
    val newtype = "0.4.4"
    val enumeratum = "1.7.0"
    val doobie = "0.13.4"
    val plivo = "4.4.1"
    val sttp = "3.3.13"
    val fs2Kafka = "1.7.0"
    val xtract = "2.2.1"
    val jwt = "5.0.0"
    val tofu = "0.10.2"
    val log4Cats = "1.1.1"
    val mouse = "1.0.3"
    val meowMtl = "0.3.0-M1"
    val scalacheckShapeless = "1.3.0"
    val testcontainersScala = "0.39.5"
    val testcontainersPostgresql = "1.16.0"
    val specs2 = "4.12.1"
  }

  val logbackClassic = "ch.qos.logback" % "logback-classic" % Versions.logback
  val catsCore = "org.typelevel" %% "cats-core" % Versions.cats
  val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect
  val shapeless = "com.chuusai" %% "shapeless" % "2.3.3"
  val aecorCore = "io.aecor" %% "core" % Versions.aecor
  val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % Versions.http4s
  val http4sCirce = "org.http4s" %% "http4s-circe" % Versions.http4s
  val http4sDsl = "org.http4s" %% "http4s-dsl" % Versions.http4s
  val monix = "io.monix" %% "monix" % Versions.monix
  val bcprov = "org.bouncycastle" % "bcprov-jdk15on" % Versions.bouncyCastle
  val bcpkix = "org.bouncycastle" % "bcpkix-jdk15on" % Versions.bouncyCastle
  val doobieCore = "org.tpolecat" %% "doobie-core" % Versions.doobie
  val doobiePostgres = "org.tpolecat" %% "doobie-postgres" % Versions.doobie
  val doobieHikari = "org.tpolecat" %% "doobie-hikari" % Versions.doobie
  val pureConfig = "com.github.pureconfig" %% "pureconfig" % Versions.pureconfig
  val pureconfigCatsEffect = "com.github.pureconfig" %% "pureconfig-cats-effect" % Versions.pureconfig
  val newtype = "io.estatico" %% "newtype" % Versions.newtype
  val enumeratum = "com.beachape" %% "enumeratum" % "1.7.0"
  val plivo = "com.plivo" % "plivo-java" % Versions.plivo
  val sttpBackendCats = "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats-ce2" % Versions.sttp
  val fs2Kafka = "com.github.fd4s" %% "fs2-kafka" % Versions.fs2Kafka
  val xtract = "com.lucidchart" %% "xtract" % Versions.xtract
  val circeGeneric = "io.circe" %% "circe-generic" % Versions.circe
  val circeParser = "io.circe" %% "circe-parser" % Versions.circe
  val jwtCirce = "com.pauldijou" %% "jwt-circe" % Versions.jwt
  val tofuCore = "tf.tofu" %% "tofu-core" % Versions.tofu
  val tofuEnv = "tf.tofu" %% "tofu-env" % Versions.tofu
  val tofuLogging = "tf.tofu" %% "tofu-logging" % Versions.tofu
  val tofuDoobie = "tf.tofu" %% "tofu-doobie" % Versions.tofu
  val log4catsCore = "io.chrisdavenport" %% "log4cats-core" % Versions.log4Cats
  val log4catsSlf4j = "io.chrisdavenport" %% "log4cats-slf4j" % Versions.log4Cats
  val mouse = "org.typelevel" %% "mouse" % Versions.mouse
  val meowMtl = "com.olegpy" %% "meow-mtl" % Versions.meowMtl
  val scalacheckShapeless = "com.github.alexarchambault" %% "scalacheck-shapeless_1.15" % Versions.scalacheckShapeless
  val testcontainersScala = "com.dimafeng" %% "testcontainers-scala" % Versions.testcontainersScala
  val testcontainersPostgresql = "org.testcontainers" % "postgresql" % Versions.testcontainersPostgresql
  val specs2Core = "org.specs2" %% "specs2-core" % Versions.specs2
}
