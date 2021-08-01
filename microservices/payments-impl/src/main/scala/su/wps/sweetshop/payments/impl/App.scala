package su.wps.sweetshop.payments.impl

import cats.effect.ExitCode
import cats.~>
import doobie._
import monix.eval.{Task, TaskApp}
import su.wps.sweetshop.payments.impl.data.AppContext
import tofu.env.Env
import tofu.logging.{LoggableContext, Logs}

object App extends TaskApp {
  type EnvF[+A] = Env[AppContext, A]

  implicit private def logs: Logs[Task, EnvF] = Logs.withContext[Task, EnvF]

  implicit private def loggableContext: LoggableContext[EnvF] =
    LoggableContext.of[EnvF].instance[AppContext]

  def run(args: List[String]): Task[ExitCode] = {
    implicit val fkICIO: Task ~> ConnectionIO = Task.liftTo[ConnectionIO]
    implicit val fkDBCIO: ConnectionIO ~> ConnectionIO =
      λ[ConnectionIO ~> ConnectionIO](identity(_))

    AppF.resource[Task, EnvF, ConnectionIO].use(_ => Task.never).as(ExitCode.Success)
  }
}
