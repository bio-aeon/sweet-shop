package su.wps.sweetshop.payments.impl.config

import pureconfig.ConfigReader
import pureconfig.generic.auto.exportReader

final case class AppConfig(dbs: DbsConfig,
                           httpServer: HttpServerConfig,
                           postgresJournals: PostgresJournalsConfig,
                           payture: PaytureConfig)

final case class DbsConfig(read: DbConfig, write: DbConfig)

final case class DbConfig(driver: String, url: String, username: String, password: String)

final case class HttpServerConfig(interface: String, port: Int)

final case class PostgresJournalsConfig(cardLinks: PostgresEventJournalConfig,
                                        customers: PostgresEventJournalConfig,
                                        payments: PostgresEventJournalConfig)

final case class PostgresEventJournalConfig(tableName: String)

final case class PaytureConfig(endpoint: String,
                               terminalAdd: String,
                               terminalPay: String,
                               password: String,
                               cryptKey: String)

object AppConfig {
  implicit val reader: ConfigReader[AppConfig] = exportReader[AppConfig].instance
}
