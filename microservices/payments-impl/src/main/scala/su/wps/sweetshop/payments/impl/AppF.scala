package su.wps.sweetshop.payments.impl

import cats.effect.{Blocker, Concurrent, ConcurrentEffect, ContextShift, Resource, Timer}
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.typesafe.config.ConfigFactory
import pureconfig.ConfigSource
import pureconfig.module.catseffect._
import su.wps.sweetshop.payments.impl.config.AppConfig
import su.wps.sweetshop.payments.impl.data.AppContext
import su.wps.sweetshop.payments.impl.wirings.EndpointWirings
import tofu.WithRun
import tofu.lift.Unlift
import tofu.logging.{Logging, Logs}
import tofu.syntax.context.runContext
import tofu.syntax.lift._
import tofu.syntax.logging._

import scala.concurrent.ExecutionContext.Implicits.global

object AppF {
  def resource[I[_]: Timer: ContextShift, F[_]: Timer: Concurrent: ContextShift](
    implicit I: ConcurrentEffect[I],
    WR: WithRun[F, I, AppContext],
    logs: Logs[I, F]
  ): Resource[I, Unit] = {
    val ctx = AppContext.empty
    for {
      implicit0(ul: Unlift[F, I]) <- Resource.eval(
        runContext(WR.subIso.map(isoK => Unlift.byIso(isoK.inverse)))(ctx)
      )
      implicit0(log: Logging[F]) <- Resource.eval(logs.forService[AppF.type])
      _ <- Resource.eval(info"Starting Application".lift[I])
      appResource: Resource[I, Unit] = for {
        appConfig <- Resource.eval(
          loadF[I, AppConfig](
            ConfigSource.fromConfig(ConfigFactory.load()),
            Blocker.liftExecutionContext(global)
          )
        )
        endpointWirings <- Resource.eval(
          EndpointWirings
            .create[I, F](appConfig)
        )
        _ <- Resource.eval(endpointWirings.launchHttpService.compile.drain)
        _ <- Resource.eval(info"Releasing application resources".lift[I])
      } yield ()
      _ <- appResource.handleErrorWith(
        err =>
          Resource
            .eval(errorCause"${err.getMessage}" (err).lift[I].flatMap(_ => I.raiseError[Unit](err)))
      )
    } yield ()
  }
}
