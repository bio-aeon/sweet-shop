package su.wps.sweetshop.payments.impl.entities

import aecor.MonadActionLiftReject
import aecor.data._
import cats.Monad
import cats.data.EitherT._
import cats.effect.Clock
import cats.syntax.all._
import io.circe.Json
import su.wps.sweetshop.payments.impl.models.PaymentId
import su.wps.sweetshop.payments.impl.models.domain.PaymentStatus
import su.wps.sweetshop.payments.impl.models.domain.PaymentStatus._
import su.wps.sweetshop.payments.impl.models.domain.es._

import java.time.Instant
import java.util.concurrent.TimeUnit

class EventsourcedPayment[F[_], I[_]](clock: Clock[F])(
  implicit I: MonadActionLiftReject[I, F, Option[PaymentState], PaymentEvent, PaymentCommandRejection]
) extends Payment[I] {

  import I._

  val ignore: I[Unit] = I.unit

  def currentTime: I[Instant] =
    liftF(clock.realTime(TimeUnit.MILLISECONDS)).map(Instant.ofEpochMilli)

  def create(userId: Int, amount: Int, details: Json): I[Unit] =
    append(PaymentCreated(userId, amount, details))

  /**
    * @todo check state before event append
    */
  def authorize: I[Unit] = append(PaymentAuthorized)

  def preCharge: I[Unit] = status.flatMap {
    case New => reject(PaymentIsNotAuthorized)
    case PreCharged | Charged => reject(PaymentIsAlreadyPreCharged)
    case Authorized => append(PaymentPreCharged)
    case Failed => reject(PaymentIsNotAuthorized)
  }

  /**
    * @todo check state before event append
    */
  def charge: I[Unit] = append(PaymentCharged)

  def fail: I[Unit] = status.flatMap {
    case Failed => ignore
    case _ => append(PaymentFailed)
  }

  def info: I[PaymentInfo] = read.flatMap {
    case Some(x) => pure(PaymentInfo(x.userId, x.amount))
    case _ => reject(PaymentNotFound)
  }

  def status: I[PaymentStatus] = read.flatMap {
    case Some(s) => pure(s.status)
    case _ => reject(PaymentNotFound)
  }
}

object EventsourcedPayment {
  def apply[I[_], F[_]](clock: Clock[F])(implicit I: MonadActionLiftReject[I, F, Option[
                                           PaymentState
                                         ], PaymentEvent, PaymentCommandRejection]): Payment[I] =
    new EventsourcedPayment(clock)

  def behavior[F[_]: Monad](clock: Clock[F]): EventsourcedBehavior[EitherK[
    Payment,
    PaymentCommandRejection,
    *[_]
  ], F, Option[PaymentState], PaymentEvent] =
    EventsourcedBehavior
      .rejectable[Payment, F, Option[PaymentState], PaymentEvent, PaymentCommandRejection](
        apply(clock),
        Fold.optional(PaymentState.fromEvent)(_.applyEvent(_))
      )

  val entityName: String = "Payment"
  val entityTag: EventTag = EventTag(entityName)
  val tagging: Tagging[PaymentId] = Tagging.partitioned(5)(entityTag)
}
