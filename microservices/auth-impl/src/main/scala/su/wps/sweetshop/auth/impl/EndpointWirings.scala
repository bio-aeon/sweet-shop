package su.wps.sweetshop.auth.impl

import cats.effect.{ConcurrentEffect, Sync, Timer}
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import su.wps.sweetshop.auth.impl.config.AppConfig

final class EndpointWirings[F[_]: ConcurrentEffect: Timer](config: AppConfig,
                                                           serviceWirings: ServiceWirings[F]) {
  import serviceWirings._

  val routes: HttpRoutes[F] = new Routes[F](smsCodeService, authService).routes

  def launchHttpService: F[Unit] =
    BlazeServerBuilder[F]
      .bindHttp(config.httpServer.port, config.httpServer.interface)
      .withHttpApp(routes.orNotFound)
      .serve
      .compile
      .drain
}

object EndpointWirings {
  def apply[F[_]: ConcurrentEffect: Timer](config: AppConfig, serviceWirings: ServiceWirings[F]) =
    Sync[F].delay(new EndpointWirings[F](config, serviceWirings))
}
