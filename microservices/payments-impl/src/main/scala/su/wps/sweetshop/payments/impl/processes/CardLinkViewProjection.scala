package su.wps.sweetshop.payments.impl.processes

import aecor.data.Folded.syntax._
import aecor.data.{Committable, ConsumerId, Enriched, Folded}
import cats.syntax.functor._
import cats.syntax.option._
import cats.{Functor, Monad}
import fs2.Stream
import io.circe.Error
import su.wps.sweetshop.payments.impl.models.CardLinkId
import su.wps.sweetshop.payments.impl.models.domain.CardLinkStatus
import su.wps.sweetshop.payments.impl.models.domain.es._
import su.wps.sweetshop.payments.impl.models.domain.views.CardLinkView
import su.wps.sweetshop.payments.impl.processes.Projection.Version
import su.wps.sweetshop.payments.impl.repositories.CardLinkViewRepository
import tofu.Throws
import tofu.lift.Lift
import tofu.logging.{Logging, Logs}
import tofu.syntax.lift._

trait CardLinkViewProjection[F[_]] {
  def run: Stream[F, Unit]
}

object CardLinkViewProjection {
  val consumerId: ConsumerId = ConsumerId("CardLinkViewProjection")

  def create[I[_]: Functor, F[_]: Monad: Throws, DB[_]: Lift[*[_], F]](
    source: ConsumerId => Stream[F, Committable[F, Either[Error, JournalEvent[CardLinkId,
                                                                              CardLinkEvent]]]],
    repo: CardLinkViewRepository[DB]
  )(implicit logs: Logs[I, F]): I[CardLinkViewProjection[F]] =
    logs
      .forService[CardLinkViewProjection[F]]
      .map(implicit log => new Impl[F, DB](source, repo))

  final private class Impl[F[_]: Monad: Throws: Logging, DB[_]: Lift[*[_], F]](
    source: ConsumerId => Stream[F, Committable[F, Either[Error, JournalEvent[CardLinkId,
                                                                              CardLinkEvent]]]],
    repo: CardLinkViewRepository[DB]
  ) extends CardLinkViewProjection[F]
      with Projection[F, JournalEvent[CardLinkId, CardLinkEvent], CardLinkView]
      with ProjectionFlow[F, CardLinkId, Enriched[EventMetadata, CardLinkEvent], CardLinkView] {

    def run: Stream[F, Unit] =
      source(consumerId).through(pipe)

    def fetchVersionAndState(
      event: JournalEvent[CardLinkId, CardLinkEvent]
    ): F[(Version, Option[CardLinkView])] =
      repo
        .findById(event.entityKey)
        .lift[F]
        .map(v => v.fold(Projection.initialVersion)(v => Version(v.version)) -> v)

    def saveNewVersion(s: CardLinkView, version: Version): F[Unit] =
      repo.set(s.copy(version = version.value)).lift[F]

    def applyEvent(
      s: Option[CardLinkView]
    )(event: JournalEvent[CardLinkId, CardLinkEvent]): Folded[Option[CardLinkView]] = s match {
      case None =>
        event.payload.event match {
          case e: CardLinkCreated =>
            Some(
              CardLinkView(
                event.entityKey,
                e.userId,
                None,
                None,
                CardLinkStatus.New,
                event.payload.metadata.dt,
                Projection.initialVersion.value
              )
            ).next
          case _ => impossible
        }

      case Some(x) =>
        event.payload.event match {
          case e: CardLinkActivated =>
            x.copy(
                extCardId = Some(e.extCardId),
                maskedPan = Some(e.maskedPan),
                status = CardLinkStatus.Active
              )
              .some
              .next
          case _ => impossible
        }
    }
  }
}
