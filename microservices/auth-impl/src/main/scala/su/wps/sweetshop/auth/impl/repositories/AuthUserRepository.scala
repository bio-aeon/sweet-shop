package su.wps.sweetshop.auth.impl.repositories

import cats.effect.Sync
import cats.syntax.functor._
import su.wps.sweetshop.auth.impl.models.AuthUser
import su.wps.sweetshop.auth.impl.repositories.sql.AuthUserSql
import tofu.doobie.LiftConnectionIO

trait AuthUserRepository[DB[_]] {
  def insert(authUser: AuthUser): DB[AuthUser]

  def findByPhone(phone: String): DB[Option[AuthUser]]
}

object AuthUserRepository {
  def create[I[_]: Sync, DB[_]: LiftConnectionIO]: I[AuthUserRepository[DB]] =
    AuthUserSql.create[I, DB].map { sql =>
      new Impl[DB](sql)
    }

  private final class Impl[DB[_]](sql: AuthUserSql[DB]) extends AuthUserRepository[DB] {
    def insert(authUser: AuthUser): DB[AuthUser] =
      sql.insert(authUser)

    def findByPhone(phone: String): DB[Option[AuthUser]] =
      sql.findByPhone(phone)
  }
}
