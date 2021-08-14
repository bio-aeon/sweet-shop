package su.wps.sweetshop.auth.impl.models

import java.time.ZonedDateTime

case class AuthUser(email: Option[String] = None,
                    password: Option[String] = None,
                    isVerified: Boolean = false,
                    createdAt: ZonedDateTime,
                    phone: Option[String] = None,
                    role: UserRole = UserRole.User,
                    id: Option[UserId] = None) {

  def pk: UserId = id.getOrElse(throw new Exception("Empty user pk."))
}
