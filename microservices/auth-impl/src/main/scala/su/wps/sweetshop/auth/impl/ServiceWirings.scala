package su.wps.sweetshop.auth.impl

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.~>
import doobie._
import su.wps.sweetshop.auth.impl.config.AppConfig
import su.wps.sweetshop.auth.impl.gateways.PlivoGateway
import su.wps.sweetshop.auth.impl.services.{
  AuthService,
  AuthServiceImpl,
  SMSCodeService,
  SMSCodeServiceImpl
}

final class ServiceWirings[F[_]](val authService: AuthService[F],
                                 val smsCodeService: SMSCodeService[F])

object ServiceWirings {
  def apply[F[_]](config: AppConfig, dbWirings: DbWirings[F])(implicit F: Sync[F],
                                                              transform: ConnectionIO ~> F) = {
    import dbWirings._

    for {
      smsGateway <- F.delay(new PlivoGateway[F](config.plivo))
      smsCodeService <- F.delay(new SMSCodeServiceImpl(smsCodeRepo, smsGateway))
      authService <- F.delay(new AuthServiceImpl(config.service.auth, smsCodeRepo, authUserRepo))
    } yield new ServiceWirings(authService, smsCodeService)
  }
}
