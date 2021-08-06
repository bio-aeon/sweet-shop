package su.wps.sweetshop.payments.impl.wirings

import cats.effect.{Concurrent, ConcurrentEffect, ExitCode, Timer}
import cats.syntax.functor._
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import su.wps.sweetshop.payments.impl.config.{AppConfig, HttpServerConfig}
import su.wps.sweetshop.payments.impl.data.AppContext
import su.wps.sweetshop.payments.impl.endpoints.Routes
import tofu.WithRun
import tofu.logging.Logs

import scala.concurrent.ExecutionContext

final class EndpointWirings[I[_]: Timer: ConcurrentEffect](config: HttpServerConfig,
                                                           routes: HttpRoutes[I]) {
  def launchHttpService: Stream[I, ExitCode] =
    BlazeServerBuilder
      .apply(ExecutionContext.global)
      .bindHttp(config.port, config.interface)
      .withHttpApp(routes.orNotFound)
      .serve
}

object EndpointWirings {
  def create[I[_]: ConcurrentEffect: Timer, F[_]: Concurrent: Timer](
    config: AppConfig,
    serviceWirings: ServiceWirings[F],
    validatorWirings: ValidatorWirings[F]
  )(implicit logs: Logs[I, F], WR: WithRun[F, I, AppContext]): I[EndpointWirings[I]] =
    Routes
      .create[I, F](config.payture, serviceWirings, validatorWirings)
      .map(_.routes)
      .map(routes => new EndpointWirings[I](config.httpServer, routes))
}
