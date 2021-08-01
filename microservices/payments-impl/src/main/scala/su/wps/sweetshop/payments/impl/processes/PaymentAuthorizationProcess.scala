package su.wps.sweetshop.payments.impl.processes

import aecor.data.{Committable, ConsumerId}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Functor, Monad}
import fs2.Stream
import io.circe.Error
import su.wps.sweetshop.payments.impl.gateways.PaytureGateway
import su.wps.sweetshop.payments.impl.models.domain.es.{
  JournalEvent,
  PaymentCreated,
  PaymentInfo,
  Payments
}
import su.wps.sweetshop.payments.impl.models.{CustomerId, PaymentId}
import su.wps.sweetshop.payments.impl.repositories.{CardLinkViewRepository, CustomerViewRepository}
import su.wps.sweetshop.utils.syntax.handle._
import tofu.lift.Lift
import tofu.logging.{Logging, Logs}
import tofu.syntax.handle._
import tofu.syntax.lift._
import tofu.syntax.logging._
import tofu.syntax.raise._
import tofu.{Catches, Throws}

trait PaymentAuthorizationProcess[F[_]] {
  def run: Stream[F, Unit]
}

object PaymentAuthorizationProcess {
  val consumerId: ConsumerId = ConsumerId("PaymentAuthorizationProcess")

  def create[I[_]: Functor, F[_]: Monad: Throws: Catches, DB[_]: Lift[*[_], F]](
    source: ConsumerId => Stream[F, Committable[F, Either[Error, JournalEvent[PaymentId,
                                                                              PaymentCreated]]]],
    paytureGateway: PaytureGateway[F],
    cardLinkViewRepo: CardLinkViewRepository[DB],
    customerViewRepo: CustomerViewRepository[DB],
    payments: Payments[F]
  )(implicit logs: Logs[I, F]): I[PaymentAuthorizationProcess[F]] =
    logs
      .forService[PaymentAuthorizationProcess[F]]
      .map(
        implicit log =>
          new Impl[F, DB](source, paytureGateway, cardLinkViewRepo, customerViewRepo, payments)
      )

  final private class Impl[F[_]: Catches: Logging, DB[_]: Lift[*[_], F]](
    source: ConsumerId => Stream[F, Committable[F, Either[Error, JournalEvent[PaymentId,
                                                                              PaymentCreated]]]],
    paytureGateway: PaytureGateway[F],
    cardLinkViewRepo: CardLinkViewRepository[DB],
    customerViewRepo: CustomerViewRepository[DB],
    payments: Payments[F]
  )(implicit F: Monad[F], R: Throws[F])
      extends PaymentAuthorizationProcess[F] {
    def run: Stream[F, Unit] =
      source(consumerId)
        .evalMap(c => handleEvent(c.value).handled >> c.commit)

    private[processes] def handleEvent(
      event: Either[Error, JournalEvent[PaymentId, PaymentCreated]]
    ) =
      event match {
        case Right(e) =>
          for {
            payment <- payments(e.entityKey).info >>= (_.fold(
              r => R.raise[PaymentInfo](new Exception(r.toString)),
              F.pure
            ))
            cardLink <- cardLinkViewRepo.findFirstByUserId(payment.userId).lift[F] >>= (_.orRaise(
              new Exception(s"User ${payment.userId} has no cards.")
            ))
            extCardId <- cardLink.extCardId.orRaise(
              new Exception(s"Incorrect card link ${cardLink.id}")
            )
            customer <- customerViewRepo
              .findById(CustomerId(payment.userId))
              .lift[F] >>= (_.orRaise(
              new Exception(s"User ${payment.userId} has no related customer.")
            ))
            paytureRes <- paytureGateway
              .initBlock(
                e.entityKey.value,
                payment.amount,
                customer.email,
                customer.password,
                extCardId
              )
              .as(true)
              .handle[Throwable](_ => false)
            _ <- handlePaytureRes(e.entityKey, paytureRes)
            _ <- payments(e.entityKey).authorize >>= (_.fold(
              r =>
                R.raise[Unit](
                  new Exception(
                    s"Payment ${e.entityKey} authorization got unexpected rejection. Reason: $r"
                  )
              ),
              _ => F.unit
            ))
          } yield ()
        case Left(err) => errorCause"Failed to decode payment event." (err)
      }

    private[processes] def handlePaytureRes(paymentId: PaymentId, paytureRes: Boolean): F[Unit] =
      F.unlessA(paytureRes) {
        payments(paymentId).fail >>= (_.fold(
          r =>
            R.raise[Unit](
              new Exception(
                s"Payment $paymentId tagging as fallen got unexpected rejection. Reason: $r"
              )
          ),
          _ => R.raise[Unit](new Exception(s"Failed to authorize payment $paymentId."))
        ))
      }
  }
}
