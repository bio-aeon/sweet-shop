package su.wps.sweetshop.auth.impl.config

final case class AppConfig(db: DbConfig,
                           service: ServiceConfig,
                           httpServer: HttpServer,
                           plivo: PlivoConfig)

final case class DbConfig(driver: String, url: String, username: String, password: String)

final case class ServiceConfig(auth: AuthConfig)

final case class AuthConfig(secretKey: String)

final case class HttpServer(interface: String, port: Int)

final case class PlivoConfig(authId: String, authToken: String)
