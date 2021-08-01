package su.wps.sweetshop.payments.impl.processes

import aecor.data.{Committable, ConsumerId}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Functor, Monad}
import fs2.Stream
import io.circe.Error
import su.wps.sweetshop.payments.api.models.ChargeRequired
import su.wps.sweetshop.payments.impl.gateways.PaytureGateway
import su.wps.sweetshop.payments.impl.models.PaymentId
import su.wps.sweetshop.payments.impl.models.domain.es.Payments
import su.wps.sweetshop.utils.syntax.handle._
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._
import tofu.{Catches, Throws}

trait PaymentChargeProcess[F[_]] {
  def run: Stream[F, Unit]
}

object PaymentChargeProcess {
  val consumerId: ConsumerId = ConsumerId("PaymentChargeProcess")

  def create[I[_]: Functor, F[_]: Monad: Throws: Catches](
    source: ConsumerId => Stream[F, Committable[F, Either[Error, ChargeRequired]]],
    paytureGateway: PaytureGateway[F],
    payments: Payments[F]
  )(implicit logs: Logs[I, F]): I[PaymentChargeProcess[F]] =
    logs
      .forService[PaymentChargeProcess[F]]
      .map(implicit log => new Impl[F](source, paytureGateway, payments))

  final private class Impl[F[_]: Catches: Logging](
    source: ConsumerId => Stream[F, Committable[F, Either[Error, ChargeRequired]]],
    paytureGateway: PaytureGateway[F],
    payments: Payments[F]
  )(implicit F: Monad[F], R: Throws[F])
      extends PaymentChargeProcess[F] {
    def run: Stream[F, Unit] =
      source(consumerId)
        .evalMap(c => handleEvent(c.value).handled >> c.commit)

    private[processes] def handleEvent(event: Either[Error, ChargeRequired]) =
      event match {
        case Right(e) =>
          val id = PaymentId(e.paymentId)
          for {
            _ <- payments(id).preCharge >>= (_.fold(
              r =>
                R.raise[Unit](
                  new Exception(s"Payment $id pre-charge got unexpected rejection. Reason: $r")
              ),
              F.pure
            ))
            _ <- paytureGateway.charge(e.paymentId)
            _ <- payments(id).charge >>= (_.fold(
              r =>
                R.raise[Unit](
                  new Exception(s"Payment $id charge got unexpected rejection. Reason: $r")
              ),
              F.pure
            ))
            _ <- info"Payment ${id.toString} charged successfully."
          } yield ()
        case Left(err) => errorCause"Failed to decode charge required event." (err)
      }
  }
}
