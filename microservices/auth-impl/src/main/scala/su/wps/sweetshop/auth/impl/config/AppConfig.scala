package su.wps.sweetshop.auth.impl.config

final case class AppConfig(plivo: PlivoConfig)

final case class PlivoConfig(authId: String, authToken: String)
