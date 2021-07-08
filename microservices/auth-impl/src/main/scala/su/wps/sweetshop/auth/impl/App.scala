package su.wps.sweetshop.auth.impl

import cats.effect.ExitCode
import cats.syntax.functor._
import monix.eval.{Task, TaskApp}
import su.wps.sweetshop.auth.impl.data.AppContext
import tofu.env.Env
import tofu.logging.{LoggableContext, Logs}

object App extends TaskApp {
  type EnvCxt[+A] = Env[AppContext, A]

  implicit private def logs: Logs[Task, EnvCxt] = Logs.withContext[Task, EnvCxt]

  implicit private def loggableContext: LoggableContext[EnvCxt] =
    LoggableContext.of[EnvCxt].instance[AppContext]

  def run(args: List[String]): Task[ExitCode] =
    new AppF[Task, EnvCxt]().resource.use(_ => Task.never).as(ExitCode.Success)
}
