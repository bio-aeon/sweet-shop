package su.wps.sweetshop.webgateway

import cats.effect.ExitCode
import cats.syntax.functor._
import monix.eval.{Task, TaskApp}

class App extends TaskApp {
  def run(args: List[String]): Task[ExitCode] =
    Task.never.as(ExitCode.Success)
}
