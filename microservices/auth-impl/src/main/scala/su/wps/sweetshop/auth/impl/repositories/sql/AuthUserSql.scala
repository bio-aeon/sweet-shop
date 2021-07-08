package su.wps.sweetshop.auth.impl.repositories.sql

import cats.effect.Sync
import cats.syntax.functor._
import cats.tagless.syntax.functorK._
import cats.tagless.{Derive, FunctorK}
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import su.wps.sweetshop.auth.impl.models.{AuthUser, UserId}
import tofu.doobie.LiftConnectionIO

trait AuthUserSql[DB[_]] {
  def insert(authUser: AuthUser): DB[AuthUser]

  def findByPhone(phone: String): DB[Option[AuthUser]]
}

object AuthUserSql extends DefaultAuthUserSql {
  implicit def functorK: FunctorK[AuthUserSql] = Derive.functorK
}

abstract sealed class DefaultAuthUserSql {
  def create[I[_]: Sync, DB[_]](implicit L: LiftConnectionIO[DB]): I[AuthUserSql[DB]] =
    Slf4jDoobieLogHandler.create[I].map(implicit logger => new Impl().mapK(L.liftF))

  private final class Impl(implicit lh: LogHandler) extends AuthUserSql[ConnectionIO] {
    val tableName: Fragment = Fragment.const("auth_users")

    def insert(authUser: AuthUser): ConnectionIO[AuthUser] =
      (fr"""
      insert into""" ++ tableName ++ fr"""(
        phone,
        created_at
      )
      values (
        ${authUser.phone},
        ${authUser.createdAt}
      )
    """).update
        .withUniqueGeneratedKeys[Int]("id")
        .map(id => authUser.copy(id = UserId(id)))

    def findByPhone(phone: String): ConnectionIO[Option[AuthUser]] =
      (fr"select email, password, is_verified, created_at, phone, role, id from" ++ tableName ++
        fr"where phone = $phone").query[AuthUser].option
  }
}
