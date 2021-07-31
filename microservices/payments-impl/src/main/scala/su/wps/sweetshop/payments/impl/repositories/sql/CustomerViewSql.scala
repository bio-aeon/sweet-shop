package su.wps.sweetshop.payments.impl.repositories.sql

import cats.effect.Sync
import cats.syntax.functor._
import cats.tagless.syntax.functorK._
import cats.tagless.{Derive, FunctorK}
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import su.wps.sweetshop.payments.impl.models.CustomerId
import su.wps.sweetshop.payments.impl.models.domain.views.CustomerView
import tofu.doobie.LiftConnectionIO

trait CustomerViewSql[DB[_]] {
  def findById(id: CustomerId): DB[Option[CustomerView]]

  def set(view: CustomerView): DB[Int]
}

object CustomerViewSql extends DefaultCustomerViewSql {
  implicit def functorK: FunctorK[CustomerViewSql] = Derive.functorK
}

abstract sealed class DefaultCustomerViewSql {
  def create[I[_]: Sync, DB[_]](implicit L: LiftConnectionIO[DB]): I[CustomerViewSql[DB]] =
    Slf4jDoobieLogHandler.create[I].map(implicit logger => new Impl().mapK(L.liftF))

  private final class Impl(implicit lh: LogHandler) extends CustomerViewSql[ConnectionIO] {
    val tableName = "customers"
    val table: Fragment = Fragment.const(tableName)

    def findById(id: CustomerId): ConnectionIO[Option[CustomerView]] =
      (fr"select * from" ++ table ++ fr"where id = $id").query[CustomerView].option

    def set(view: CustomerView): ConnectionIO[Int] =
      Update[CustomerView](setViewQuery).run(view)

    private val setViewQuery =
      s"""insert into $tableName
      (id, email, password, status, created_at, version)
      values (?, ?, ?, ?, ?, ?)
      on conflict (id)
      do update set
       email = excluded.email,
       password = excluded.password,
       status = excluded.status,
       created_at = excluded.created_at,
       version = excluded.version;"""
  }
}
