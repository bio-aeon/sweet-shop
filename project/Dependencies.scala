import sbt._

object Dependencies {
  val Http4sVersion = "0.20.12"
  val CirceVersion = "0.11.1"
  val Specs2Version = "4.8.0"
  val LogbackVersion = "1.2.3"
  val MonixVersion = "3.0.0"
  val DoobieVersion = "0.6.0"
  val PureConfigVersion = "0.10.2"
  val PlivoVersion = "4.4.1"

  lazy val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % Http4sVersion
  lazy val http4sCirce = "org.http4s" %% "http4s-circe" % Http4sVersion
  lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % Http4sVersion
  lazy val monix = "io.monix" %% "monix" % MonixVersion
  lazy val doobieCore = "org.tpolecat" %% "doobie-core" % DoobieVersion
  lazy val doobiePostgres = "org.tpolecat" %% "doobie-postgres" % DoobieVersion
  lazy val doobieHikari = "org.tpolecat" %% "doobie-hikari" % DoobieVersion
  lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % PureConfigVersion
  lazy val plivo = "com.plivo" % "plivo-java" % PlivoVersion
}
