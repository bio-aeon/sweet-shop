package su.wps.sweetshop.auth.api.models

final case class CreateTokenBySMSCodeRequest(phone: String, code: String)
