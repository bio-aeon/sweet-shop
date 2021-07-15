package su.wps.sweetshop.payments.impl

import cats.effect.ExitCode
import monix.eval.{Task, TaskApp}
import su.wps.sweetshop.payments.impl.data.AppContext
import tofu.env.Env
import tofu.logging.{LoggableContext, Logs}

object App extends TaskApp {
  type EnvCxt[+A] = Env[AppContext, A]

  implicit private def logs: Logs[Task, EnvCxt] = Logs.withContext[Task, EnvCxt]

  implicit private def loggableContext: LoggableContext[EnvCxt] =
    LoggableContext.of[EnvCxt].instance[AppContext]

  def run(args: List[String]): Task[ExitCode] =
    AppF.resource[Task, EnvCxt].use(_ => Task.never).as(ExitCode.Success)
}
