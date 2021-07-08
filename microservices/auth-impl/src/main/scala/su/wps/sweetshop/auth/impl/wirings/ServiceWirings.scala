package su.wps.sweetshop.auth.impl.wirings

import cats.Monad
import cats.effect.{Clock, Sync}
import cats.syntax.functor._
import cats.syntax.flatMap._
import su.wps.sweetshop.auth.impl.config.AppConfig
import su.wps.sweetshop.auth.impl.gateways.PlivoGateway
import su.wps.sweetshop.auth.impl.services.{AuthService, SMSCodeService}
import tofu.doobie.transactor.Txr
import tofu.logging.Logs

final case class ServiceWirings[F[_]](authService: AuthService[F],
                                      smsCodeService: SMSCodeService[F])

object ServiceWirings {
  def create[I[_]: Sync, F[_]: Clock: Sync, DB[_]: Monad](
    config: AppConfig,
    repoWirings: RepositoryWirings[DB],
    xa: Txr.Aux[F, DB]
  )(implicit logs: Logs[I, F]): I[ServiceWirings[F]] = {
    import repoWirings._

    for {
      smsGateway <- PlivoGateway.create[I, F](config.plivo)
      smsCodeService <- SMSCodeService.create[I, F, DB](smsCodeRepo, smsGateway, xa)
      authService <- AuthService.create[I, F, DB](
        config.service.auth,
        smsCodeRepo,
        authUserRepo,
        xa
      )
    } yield ServiceWirings(authService, smsCodeService)
  }
}
