package su.wps.sweetshop.auth.impl.repositories

import doobie._
import doobie.implicits._
import su.wps.sweetshop.auth.impl.models.{AuthUser, UserId}

class AuthUserRepositoryImpl extends DoobieRepository with AuthUserRepository[ConnectionIO] {
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
