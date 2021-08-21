package su.wps.sweetshop.auth.impl.wirings

import cats.effect.{Async, Blocker, ContextShift, Resource, Sync}
import com.zaxxer.hikari.HikariDataSource
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import mouse.any._
import su.wps.sweetshop.auth.impl.config.DbConfig
import su.wps.sweetshop.utils.syntax.resource._
import tofu.doobie.transactor.Txr

object TransactorWirings {

  def resource[F[_]: ContextShift: Async](config: DbConfig): Resource[F, Txr.Continuational[F]] =
    for {
      txr <- mkTransactor(config)
      _ <- txr.connect(txr.kernel)
    } yield Txr.continuational(txr)

  private def mkTransactor[F[_]: ContextShift: Async, DB[_]](
    config: DbConfig
  ): Resource[F, HikariTransactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](32)
      te <- ExecutionContexts.cachedThreadPool[F]
      tr <- HikariTransactor
        .newHikariTransactor[F](
          config.driver,
          config.url,
          config.username,
          config.password,
          ce,
          Blocker.liftExecutionContext(te)
        )
      _ <- tr.configure(tr => configureDataSource[F](tr)).toResource
    } yield tr

  private def configureDataSource[F[_]](ds: HikariDataSource)(implicit F: Sync[F]): F[Unit] =
    F.delay {
      ds.setMinimumIdle(8) |> (_ => ds.setMaximumPoolSize(32)) |> (_ => ds.setAutoCommit(false)) |> (
        _ => ds.setInitializationFailTimeout(-1)
      )
    }
}
