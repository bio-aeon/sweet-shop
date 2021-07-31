package su.wps.sweetshop.payments.impl.repositories

import cats.Functor
import cats.effect.Sync
import cats.syntax.functor._
import su.wps.sweetshop.payments.impl.models.CustomerId
import su.wps.sweetshop.payments.impl.models.domain.views.CustomerView
import su.wps.sweetshop.payments.impl.repositories.sql.CustomerViewSql
import tofu.doobie.LiftConnectionIO

trait CustomerViewRepository[DB[_]] {
  def findById(id: CustomerId): DB[Option[CustomerView]]

  def set(view: CustomerView): DB[Unit]
}

object CustomerViewRepository {
  def create[I[_]: Sync, DB[_]: LiftConnectionIO: Functor]: I[CustomerViewRepository[DB]] =
    CustomerViewSql.create[I, DB].map { sql =>
      new Impl[DB](sql)
    }

  private final class Impl[DB[_]: Functor](sql: CustomerViewSql[DB])
      extends CustomerViewRepository[DB] {
    def findById(id: CustomerId): DB[Option[CustomerView]] =
      sql.findById(id)

    def set(view: CustomerView): DB[Unit] =
      sql.set(view).void
  }
}
