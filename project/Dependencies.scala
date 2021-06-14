import sbt._

object Dependencies {
  val CatsVersion = "2.6.1"
  val Http4sVersion = "0.21.24"
  val CirceVersion = "0.13.0"
  val Specs2Version = "4.12.1"
  val LogbackVersion = "1.2.3"
  val MonixVersion = "3.0.0"
  val DoobieVersion = "0.10.0"
  val PureConfigVersion = "0.14.0"
  val PlivoVersion = "4.4.1"
  val JwtVersion = "5.0.0"

  lazy val catsCore = "org.typelevel" %% "cats-core" % CatsVersion
  lazy val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % Http4sVersion
  lazy val http4sCirce = "org.http4s" %% "http4s-circe" % Http4sVersion
  lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % Http4sVersion
  lazy val monix = "io.monix" %% "monix" % MonixVersion
  lazy val doobieCore = "org.tpolecat" %% "doobie-core" % DoobieVersion
  lazy val doobiePostgres = "org.tpolecat" %% "doobie-postgres" % DoobieVersion
  lazy val doobieHikari = "org.tpolecat" %% "doobie-hikari" % DoobieVersion
  lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % PureConfigVersion
  lazy val pureconfigCatsEffect = "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion
  lazy val plivo = "com.plivo" % "plivo-java" % PlivoVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % CirceVersion
  lazy val circeOptics = "io.circe" %% "circe-optics" % CirceVersion
  lazy val jwtCirce = "com.pauldijou" %% "jwt-circe" % JwtVersion
}
