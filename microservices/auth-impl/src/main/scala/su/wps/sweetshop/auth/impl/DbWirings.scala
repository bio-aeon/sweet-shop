package su.wps.sweetshop.auth.impl

import cats.Monad
import cats.effect.{Async, ContextShift, Resource, Timer}
import doobie.util.transactor.Transactor
import su.wps.sweetshop.auth.impl.config.DbConfig
import su.wps.sweetshop.auth.impl.postgres.PostgresTransactor
import su.wps.sweetshop.auth.impl.repositories.{AuthUserRepositoryImpl, SMSCodeRepositoryImpl}

final class DbWirings[F[_]: Monad](val xa: Transactor[F]) {
  val smsCodeRepo = new SMSCodeRepositoryImpl
  val authUserRepo = new AuthUserRepositoryImpl
}

object DbWirings {
  def apply[F[_]: Async: Timer: ContextShift](settings: DbConfig): Resource[F, DbWirings[F]] =
    for {
      transactor <- PostgresTransactor.transactor[F](settings)
      wirings = new DbWirings(transactor)
    } yield wirings
}
