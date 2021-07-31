package su.wps.sweetshop.payments.impl.entities

import aecor.MonadActionReject
import aecor.data._
import cats.Monad
import cats.syntax.all._
import su.wps.sweetshop.payments.impl.models.CustomerId
import su.wps.sweetshop.payments.impl.models.domain.es._

class EventsourcedCustomer[F[_]](
  implicit F: MonadActionReject[F, Option[CustomerState], CustomerEvent, CustomerCommandRejection]
) extends Customer[F] {

  import F._

  val ignore: F[Unit] = F.unit

  def create(email: String, password: String): F[Unit] =
    append(CustomerCreated(email, password))

  def register: F[Unit] = append(CustomerRegistered)

  def info: F[CustomerInfo] = read.flatMap {
    case Some(s) => pure(CustomerInfo(s.email, s.password))
    case _ => reject(CustomerNotFound)
  }
}

object EventsourcedCustomer {
  def apply[F[_]: MonadActionReject[*[_],
                                    Option[CustomerState],
                                    CustomerEvent,
                                    CustomerCommandRejection]]: Customer[F] =
    new EventsourcedCustomer

  def behavior[F[_]: Monad]
    : EventsourcedBehavior[EitherK[Customer, CustomerCommandRejection, *[_]], F, Option[
      CustomerState
    ], CustomerEvent] =
    EventsourcedBehavior
      .rejectable[Customer, F, Option[CustomerState], CustomerEvent, CustomerCommandRejection](
        apply,
        Fold.optional(CustomerState.fromEvent)(_.applyEvent(_))
      )

  val entityName: String = "Customer"
  val entityTag: EventTag = EventTag(entityName)
  val tagging: Tagging[CustomerId] = Tagging.partitioned(5)(entityTag)
}
