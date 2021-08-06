package su.wps.sweetshop.payments.impl

import cats.arrow.FunctionK
import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Resource, Timer}
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Functor, Parallel, ~>}
import doobie._
import su.wps.sweetshop.payments.impl.data.AppContext
import su.wps.sweetshop.payments.impl.wirings._
import su.wps.sweetshop.utils.syntax.resource._
import tofu.WithRun
import tofu.doobie.LiftConnectionIO
import tofu.lift.{IsoK, Unlift}
import tofu.logging.{Logging, Logs}
import tofu.syntax.context.runContext
import tofu.syntax.lift._
import tofu.syntax.logging._

object AppF {
  def resource[I[_]: Timer: ContextShift, F[_]: Timer: Concurrent: ContextShift: Parallel, DB[_]: Functor: LiftConnectionIO](
    implicit I: ConcurrentEffect[I],
    WR: WithRun[F, I, AppContext],
    fkICIO: I ~> ConnectionIO,
    fkDBCIO: DB ~> ConnectionIO,
    logs: Logs[I, F]
  ): Resource[I, Unit] = {
    val ctx = AppContext.empty
    for {
      implicit0(ul: Unlift[F, I]) <- runContext(WR.subIso.map(isoK => Unlift.byIso(isoK.inverse)))(
        ctx
      ).toResource
      implicit0(isoK: IsoK[I, F]) <- WR.subIso.toResource.mapK(ul.liftF)
      implicit0(toConnectionIO: FunctionK[F, ConnectionIO]) = λ[F ~> ConnectionIO](
        x => fkICIO(WR.runContext(x)(ctx))
      )
      implicit0(log: Logging[F]) <- logs.forService[AppF.type].toResource
      _ <- info"Starting Application".lift[I].toResource
      appResource: Resource[I, Unit] = for {
        commonWirings <- CommonWirings.create[I, F].toResource
        appConfig = commonWirings.appConfig
        dbWirings <- DbWirings.resource[I, F, DB](appConfig)
        entityWirings = EntityWirings.create[F, DB](dbWirings)
        serviceWirings <- ServiceWirings
          .create[I, F, DB](commonWirings, dbWirings, entityWirings)
          .toResource
        kafkaWirings = KafkaWirings.create[I, F](appConfig.kafka)
        processWirings <- ProcessWirings
          .create[I, F, DB](dbWirings, kafkaWirings, serviceWirings, entityWirings)
          .toResource
        validatorWirings = ValidatorWirings.create[F]
        endpointWirings <- EndpointWirings
          .create[I, F](appConfig, serviceWirings, validatorWirings)
          .toResource
        _ <- processWirings.launchProcesses.mapK(WR.runContextK(ctx))
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
