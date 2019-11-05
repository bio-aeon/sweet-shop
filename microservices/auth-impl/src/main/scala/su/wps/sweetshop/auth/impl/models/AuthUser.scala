package su.wps.sweetshop.auth.impl.models

import java.time.ZonedDateTime

case class AuthUser(id: UserId, phone: String, createdAt: ZonedDateTime)

case class UserId(value: Int) extends AnyVal
