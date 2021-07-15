package su.wps.sweetshop.payments.impl.config

import pureconfig.ConfigReader
import pureconfig.generic.auto.exportReader

final case class AppConfig(httpServer: HttpServerConfig)

final case class HttpServerConfig(interface: String, port: Int)

object AppConfig {
  implicit val reader: ConfigReader[AppConfig] = exportReader[AppConfig].instance
}
