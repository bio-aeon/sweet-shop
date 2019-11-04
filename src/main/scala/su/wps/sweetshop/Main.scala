package su.wps.sweetshop

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) =
    SweetshopServer.stream[IO].compile.drain.as(ExitCode.Success)
}