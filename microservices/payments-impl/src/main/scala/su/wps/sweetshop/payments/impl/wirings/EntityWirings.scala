package su.wps.sweetshop.payments.impl.wirings

import aecor.runtime.postgres.PostgresRuntime
import aecor.runtime.{Eventsourced, Snapshotting}
import cats.effect.{Clock, Sync, Timer}
import cats.syntax.functor._
import cats.~>
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import mouse.any._
import su.wps.sweetshop.payments.impl.entities.{
  EventsourcedCardLink,
  EventsourcedCustomer,
  EventsourcedPayment
}
import su.wps.sweetshop.payments.impl.models.domain.es._
import su.wps.sweetshop.payments.impl.models.{CardLinkId, CustomerId, PaymentId}
import su.wps.sweetshop.utils.syntax.clock._

final case class EntityWirings[F[_]](cardLinks: CardLinks[F],
                                     customers: Customers[F],
                                     payments: Payments[F])

object EntityWirings {
  def create[F[_]: Sync: Timer, DB[_]](
    dbWirings: DbWirings[F, DB]
  )(implicit toCIO: F ~> ConnectionIO): EntityWirings[F] = {
    import dbWirings._

    val generateTimestamp: F[EventMetadata] = {
      Clock[F].realZonedDt.map(EventMetadata(_))
    }

    val cardLinksBehavior =
      EventsourcedCardLink.behavior[ConnectionIO].enrich[EventMetadata](toCIO(generateTimestamp))

    val cardLinks: CardLinks[F] =
      PostgresRuntime(
        EventsourcedCardLink.entityName,
        cardLinksBehavior,
        cardLinksSchema.journal(EventsourcedCardLink.tagging),
        Snapshotting.disabled[F, CardLinkId, Option[CardLinkState]],
        writeXa
      ) |> (Eventsourced.Entities.rejectable(_))

    val customersBehavior =
      EventsourcedCustomer.behavior[ConnectionIO].enrich[EventMetadata](toCIO(generateTimestamp))

    val customers: Customers[F] =
      PostgresRuntime(
        EventsourcedCustomer.entityName,
        customersBehavior,
        customersSchema.journal(EventsourcedCustomer.tagging),
        Snapshotting.disabled[F, CustomerId, Option[CustomerState]],
        writeXa
      ) |> (Eventsourced.Entities.rejectable(_))

    val paymentsBehavior =
      EventsourcedPayment
        .behavior[ConnectionIO](Clock[F].mapK(toCIO))
        .enrich[EventMetadata](toCIO(generateTimestamp))

    val payments: Payments[F] =
      PostgresRuntime(
        EventsourcedPayment.entityName,
        paymentsBehavior,
        paymentsSchema.journal(EventsourcedPayment.tagging),
        Snapshotting.disabled[F, PaymentId, Option[PaymentState]],
        writeXa
      ) |> (Eventsourced.Entities.rejectable(_))

    EntityWirings(cardLinks, customers, payments)
  }

}
