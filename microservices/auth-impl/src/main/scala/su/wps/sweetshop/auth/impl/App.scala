package su.wps.sweetshop.auth.impl

import cats.effect.ExitCode
import cats.syntax.functor._
import monix.eval.{Task, TaskApp}

object App extends TaskApp {
  def run(args: List[String]): Task[ExitCode] = {
    val app = new AppF[Task]
    app.run.use(_ => Task.never).as(ExitCode.Success)
  }
}
