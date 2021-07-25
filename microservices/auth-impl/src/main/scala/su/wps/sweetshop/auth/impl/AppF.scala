package su.wps.sweetshop.auth.impl

import cats.effect.{Blocker, Concurrent, ConcurrentEffect, ContextShift, Resource, Timer}
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.typesafe.config.ConfigFactory
import pureconfig.ConfigSource
import pureconfig.module.catseffect._
import su.wps.sweetshop.auth.impl.config.AppConfig
import su.wps.sweetshop.auth.impl.data.AppContext
import su.wps.sweetshop.auth.impl.wirings.{
  EndpointWirings,
  RepositoryWirings,
  ServiceWirings,
  TransactorWirings
}
import su.wps.sweetshop.utils.syntax.resource._
import tofu.WithRun
import tofu.lift.Unlift
import tofu.logging.{Logging, Logs}
import tofu.syntax.context.runContext
import tofu.syntax.lift._
import tofu.syntax.logging._

import scala.concurrent.ExecutionContext.Implicits.global

class AppF[I[_]: Timer: ContextShift, F[_]: Timer: Concurrent: ContextShift](
  implicit I: ConcurrentEffect[I],
  WR: WithRun[F, I, AppContext],
  logs: Logs[I, F]
) {
  def resource: Resource[I, Unit] = {
    val ctx = AppContext.empty
    for {
      implicit0(ul: Unlift[F, I]) <- runContext(WR.subIso.map(isoK => Unlift.byIso(isoK.inverse)))(
        ctx
      ).toResource
      implicit0(log: Logging[F]) <- logs.forService[AppF[I, F]].toResource
      _ <- info"Starting Application".lift[I].toResource
      appResource: Resource[I, Unit] = for {
        appConfig <- loadF[I, AppConfig](
          ConfigSource.fromConfig(ConfigFactory.load()),
          Blocker.liftExecutionContext(global)
        ).toResource
        xa <- TransactorWirings.resource[F](appConfig.db).mapK(ul.liftF)
        repoWirings <- RepositoryWirings.create[I, xa.DB].toResource
        serviceWirings <- ServiceWirings.create[I, F, xa.DB](appConfig, repoWirings, xa).toResource
        endpointWirings <- EndpointWirings
          .create[I, F](appConfig, serviceWirings)
          .toResource
        _ <- endpointWirings.launchHttpService.compile.drain.toResource
        _ <- info"Releasing application resources".lift[I].toResource
      } yield ()
      _ <- appResource.handleErrorWith(
        err =>
          Resource
            .eval(errorCause"${err.getMessage}" (err).lift[I].flatMap(_ => I.raiseError[Unit](err)))
      )
    } yield ()
  }
}
