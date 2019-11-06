package su.wps.sweetshop.auth.impl.services

import su.wps.sweetshop.auth.api.dto.TokenDto

trait AuthService[F[_]] {
  def createTokenBySMSCode(phone: String, code: String): F[Either[String, TokenDto]]
}
