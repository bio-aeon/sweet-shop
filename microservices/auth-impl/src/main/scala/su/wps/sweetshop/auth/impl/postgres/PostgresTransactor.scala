package su.wps.sweetshop.auth.impl.postgres

import cats.effect.{Async, ContextShift, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import su.wps.sweetshop.auth.impl.config.DbConfig

object PostgresTransactor {
  def transactor[F[_]](config: DbConfig)(implicit F: Async[F],
                                         C: ContextShift[F]): Resource[F, HikariTransactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](32)
      te <- ExecutionContexts.cachedThreadPool[F]
      tr <- HikariTransactor
        .newHikariTransactor[F](config.driver, config.url, config.username, config.password, ce, te)
      _ <- Resource.liftF(tr.configure(ds => F.delay(ds.setAutoCommit(false))))
    } yield tr
}
