import sbt._

object Dependencies {
  val CatsVersion = "1.6.0"
  val Http4sVersion = "0.20.12"
  val CirceVersion = "0.11.0"
  val Specs2Version = "4.8.0"
  val LogbackVersion = "1.2.3"
  val MonixVersion = "3.0.0"
  val DoobieVersion = "0.6.0"
  val PureConfigVersion = "0.12.1"
  val PlivoVersion = "4.4.1"
  val JwtVersion = "2.1.0"
  val SangriaVersion = "1.4.2"
  val SangriaCirceVersion = "1.2.1"

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
  lazy val circeJava8 = "io.circe" %% "circe-java8" % CirceVersion
  lazy val circeOptics = "io.circe" %% "circe-optics" % CirceVersion
  lazy val jwtCirce = "com.pauldijou" %% "jwt-circe" % JwtVersion
  lazy val sangria = "org.sangria-graphql" %% "sangria" % SangriaVersion
  lazy val sangriaCirce = "org.sangria-graphql" %% "sangria-circe" % SangriaCirceVersion
}
