package su.wps.sweetshop.auth.impl.config

import pureconfig.ConfigReader
import pureconfig.generic.auto.exportReader

final case class AppConfig(db: DbConfig,
                           service: ServiceConfig,
                           httpServer: HttpServerConfig,
                           plivo: PlivoConfig)

final case class DbConfig(driver: String, url: String, username: String, password: String)

final case class ServiceConfig(auth: AuthConfig)

final case class AuthConfig(secretKey: String)

final case class HttpServerConfig(interface: String, port: Int)

final case class PlivoConfig(authId: String, authToken: String, sourcePhone: String)

object AppConfig {
  implicit val reader: ConfigReader[AppConfig] = exportReader[AppConfig].instance
}
