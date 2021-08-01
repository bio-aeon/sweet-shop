package su.wps.sweetshop.payments.impl.processes

import aecor.data.Folded.syntax._
import aecor.data.{Committable, ConsumerId, Enriched, Folded}
import cats.syntax.functor._
import cats.syntax.option._
import cats.{Functor, Monad}
import fs2.Stream
import io.circe.Error
import su.wps.sweetshop.payments.impl.models.CustomerId
import su.wps.sweetshop.payments.impl.models.domain.CustomerStatus
import su.wps.sweetshop.payments.impl.models.domain.es._
import su.wps.sweetshop.payments.impl.models.domain.views.CustomerView
import su.wps.sweetshop.payments.impl.processes.Projection.Version
import su.wps.sweetshop.payments.impl.repositories.CustomerViewRepository
import tofu.Throws
import tofu.lift.Lift
import tofu.logging.{Logging, Logs}
import tofu.syntax.lift._

trait CustomerViewProjection[F[_]] {
  def run: Stream[F, Unit]
}

object CustomerViewProjection {
  val consumerId: ConsumerId = ConsumerId("CustomerViewProjection")

  def create[I[_]: Functor, F[_]: Monad: Throws, DB[_]: Lift[*[_], F]](
    source: ConsumerId => Stream[F, Committable[F, Either[Error, JournalEvent[CustomerId,
                                                                              CustomerEvent]]]],
    repo: CustomerViewRepository[DB]
  )(implicit logs: Logs[I, F]): I[CustomerViewProjection[F]] =
    logs
      .forService[CardLinkViewProjection[F]]
      .map(implicit log => new Impl[F, DB](source, repo))

  final private class Impl[F[_]: Monad: Throws: Logging, DB[_]: Lift[*[_], F]](
    source: ConsumerId => Stream[F, Committable[F, Either[Error, JournalEvent[CustomerId,
                                                                              CustomerEvent]]]],
    repo: CustomerViewRepository[DB]
  ) extends CustomerViewProjection[F]
      with Projection[F, JournalEvent[CustomerId, CustomerEvent], CustomerView]
      with ProjectionFlow[F, CustomerId, Enriched[EventMetadata, CustomerEvent], CustomerView] {

    def run: Stream[F, Unit] =
      source(consumerId).through(pipe)

    def fetchVersionAndState(
      event: JournalEvent[CustomerId, CustomerEvent]
    ): F[(Version, Option[CustomerView])] =
      repo
        .findById(event.entityKey)
        .lift[F]
        .map(v => v.fold(Projection.initialVersion)(v => Version(v.version)) -> v)

    def saveNewVersion(s: CustomerView, version: Version): F[Unit] =
      repo.set(s.copy(version = version.value)).lift[F]

    def applyEvent(
      s: Option[CustomerView]
    )(event: JournalEvent[CustomerId, CustomerEvent]): Folded[Option[CustomerView]] = s match {
      case None =>
        event.payload.event match {
          case e: CustomerCreated =>
            Some(
              CustomerView(
                event.entityKey,
                e.email,
                e.password,
                CustomerStatus.New,
                event.payload.metadata.dt,
                Projection.initialVersion.value
              )
            ).next
          case _ => impossible
        }

      case Some(x) =>
        event.payload.event match {
          case CustomerRegistered =>
            x.copy(status = CustomerStatus.Registered).some.next
          case _ => impossible
        }
    }
  }
}
