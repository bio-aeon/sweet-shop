package su.wps.sweetshop.auth.impl.repositories

import su.wps.sweetshop.auth.impl.models.AuthUser

trait AuthUserRepository[F[_]] {
  def insert(authUser: AuthUser): F[AuthUser]

  def findByPhone(phone: String): F[Option[AuthUser]]
}
