package su.wps.sweetshop.auth.api.requests

case class CreateTokenBySMSCodeRequest(phone: String, code: String)
