package su.wps.sweetshop.payments.impl.entities

import aecor.MonadActionReject
import aecor.data._
import cats.Monad
import cats.syntax.flatMap._
import su.wps.sweetshop.payments.impl.models.CardLinkId
import su.wps.sweetshop.payments.impl.models.domain.CardLinkStatus
import su.wps.sweetshop.payments.impl.models.domain.CardLinkStatus.{Active, New}
import su.wps.sweetshop.payments.impl.models.domain.es._

class EventsourcedCardLink[F[_]](
  implicit F: MonadActionReject[F, Option[CardLinkState], CardLinkEvent, CardLinkCommandRejection]
) extends CardLink[F] {
  import F._

  val ignore: F[Unit] = F.unit

  def create(userId: Int): F[Unit] =
    append(CardLinkCreated(userId))

  def activate(maskedPan: String,
               extCardId: String,
               cardHolder: String,
               expDate: String,
               linkedAt: String): F[Unit] =
    status.flatMap {
      case New => append(CardLinkActivated(maskedPan, extCardId, cardHolder, expDate, linkedAt))
      case Active => ignore
    }

  def status: F[CardLinkStatus] = read.flatMap {
    case Some(s) => pure(s.status)
    case _ => reject(CardLinkNotFound)
  }
}

object EventsourcedCardLink {
  def apply[F[_]: MonadActionReject[*[_],
                                    Option[CardLinkState],
                                    CardLinkEvent,
                                    CardLinkCommandRejection]]: CardLink[F] =
    new EventsourcedCardLink

  def behavior[F[_]: Monad]
    : EventsourcedBehavior[EitherK[CardLink, CardLinkCommandRejection, *[_]], F, Option[
      CardLinkState
    ], CardLinkEvent] =
    EventsourcedBehavior
      .rejectable[CardLink, F, Option[CardLinkState], CardLinkEvent, CardLinkCommandRejection](
        apply,
        Fold.optional(CardLinkState.fromEvent)(_.applyEvent(_))
      )

  val entityName: String = "CardLink"
  val entityTag: EventTag = EventTag(entityName)
  val tagging: Tagging[CardLinkId] = Tagging.partitioned(5)(entityTag)
}
