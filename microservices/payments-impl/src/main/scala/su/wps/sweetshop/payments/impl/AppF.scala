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
import su.wps.sweetshop.utils.syntax.resource._
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
      implicit0(ul: Unlift[F, I]) <- runContext(WR.subIso.map(isoK => Unlift.byIso(isoK.inverse)))(
        ctx
      ).toResource
      implicit0(log: Logging[F]) <- logs.forService[AppF.type].toResource
      _ <- info"Starting Application".lift[I].toResource
      appResource: Resource[I, Unit] = for {
        appConfig <- loadF[I, AppConfig](
          ConfigSource.fromConfig(ConfigFactory.load()),
          Blocker.liftExecutionContext(global)
        ).toResource
        endpointWirings <- EndpointWirings.create[I, F](appConfig).toResource
        _ <- endpointWirings.launchHttpService.compile.drain.toResource
        _ <- info"Releasing application resources".lift[I].toResource
      } yield ()
      _ <- appResource.handleErrorWith(
        err =>
          errorCause"${err.getMessage}" (err)
            .lift[I]
            .flatMap(_ => I.raiseError[Unit](err))
            .toResource
      )
    } yield ()
  }
}
