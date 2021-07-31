package su.wps.sweetshop.payments.impl.wirings

import aecor.data.Enriched
import aecor.journal.postgres.JournalSchema
import cats.Functor
import cats.effect._
import doobie.Transactor
import com.zaxxer.hikari.HikariDataSource
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import su.wps.sweetshop.payments.impl.config.{AppConfig, DbConfig, PostgresJournalsConfig}
import su.wps.sweetshop.payments.impl.models.domain.es.{
  CardLinkEvent,
  CustomerEvent,
  EventMetadata,
  PaymentEvent
}
import su.wps.sweetshop.payments.impl.models.{CardLinkId, CustomerId, PaymentId}
import su.wps.sweetshop.payments.impl.repositories.{CardLinkViewRepository, CustomerViewRepository}
import su.wps.sweetshop.payments.impl.serialization.JournalEventSerializers.{
  CardLinkEventSerializer,
  CustomerEventSerializer,
  PaymentEventSerializer
}
import su.wps.sweetshop.utils.syntax.resource._
import tofu.doobie.LiftConnectionIO
import tofu.lift.IsoK

final class DbWirings[F[_]: Async: Timer, DB[_]] private (
  val readXa: Transactor[F],
  val writeXa: Transactor[F],
  val journals: PostgresJournalsConfig,
  val cardLinkViewRepo: CardLinkViewRepository[DB],
  val customerViewRepo: CustomerViewRepository[DB]
) {
  val cardLinksSchema: JournalSchema[CardLinkId, Enriched[EventMetadata, CardLinkEvent]] =
    JournalSchema[CardLinkId, Enriched[EventMetadata, CardLinkEvent]](
      journals.cardLinks.tableName,
      CardLinkEventSerializer
    )

  val customersSchema: JournalSchema[CustomerId, Enriched[EventMetadata, CustomerEvent]] =
    JournalSchema[CustomerId, Enriched[EventMetadata, CustomerEvent]](
      journals.customers.tableName,
      CustomerEventSerializer
    )

  val paymentsSchema: JournalSchema[PaymentId, Enriched[EventMetadata, PaymentEvent]] =
    JournalSchema[PaymentId, Enriched[EventMetadata, PaymentEvent]](
      journals.payments.tableName,
      PaymentEventSerializer
    )
}

object DbWirings {
  def resource[I[_]: Async: ContextShift, F[_]: Timer: Async, DB[_]: LiftConnectionIO: Functor](
    config: AppConfig
  )(implicit isoK: IsoK[I, F]): Resource[I, DbWirings[F, DB]] =
    for {
      readXa <- mkTransactor[I, F](config.dbs.read)
      writeXa <- mkTransactor[I, F](config.dbs.write)
      cardLinkViewRepo <- CardLinkViewRepository.create[I, DB].toResource
      customerViewRepo <- CustomerViewRepository.create[I, DB].toResource
    } yield
      new DbWirings(readXa, writeXa, config.postgresJournals, cardLinkViewRepo, customerViewRepo)

  private def mkTransactor[I[_]: ContextShift, F[_]: Sync](
    config: DbConfig
  )(implicit I: Async[I], isoK: IsoK[I, F]): Resource[I, Transactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[I](32)
      te <- ExecutionContexts.cachedThreadPool[I]
      tr <- HikariTransactor
        .newHikariTransactor[I](
          config.driver,
          config.url,
          config.username,
          config.password,
          ce,
          Blocker.liftExecutionContext(te)
        )
      _ <- tr.configure(tr => configureDataSource[I](tr)).toResource
    } yield tr.mapK(isoK.tof)

  private def configureDataSource[I[_]](ds: HikariDataSource)(implicit I: Sync[I]): I[Unit] =
    I.delay(ds.setAutoCommit(false))
}
