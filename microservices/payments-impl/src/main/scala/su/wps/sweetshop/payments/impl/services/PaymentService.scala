package su.wps.sweetshop.payments.impl.services

import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Defer, Functor, Monad}
import su.wps.sweetshop.payments.api.models.CreatePaymentRequest
import su.wps.sweetshop.payments.impl.models.domain.es.{CustomerInfo, Customers, Payments}
import su.wps.sweetshop.payments.impl.models.{CustomerId, PaymentId}
import su.wps.sweetshop.utils.syntax.uuid._
import tofu.Throws
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._

trait PaymentService[F[_]] {
  def createPayment(request: CreatePaymentRequest): F[String]
}

object PaymentService {
  def create[I[_]: Functor, F[_]: Defer: Monad: Throws](
    customers: Customers[F],
    payments: Payments[F]
  )(implicit logs: Logs[I, F]): I[PaymentService[F]] =
    logs
      .forService[PaymentService[F]]
      .map(implicit log => new Impl[F](customers, payments))

  private final class Impl[F[_]: Defer: Logging](customers: Customers[F], payments: Payments[F])(
    implicit F: Monad[F],
    R: Throws[F]
  ) extends PaymentService[F] {
    def createPayment(request: CreatePaymentRequest): F[String] =
      for {
        _ <- customers(CustomerId(request.userId)).info >>= (_.fold(
          r => R.raise[CustomerInfo](new Exception(r.toString)),
          F.pure
        ))
        id <- randomUUIDString
        _ <- payments(PaymentId(id))
          .create(request.userId, request.amount, request.details) >>= (_.fold(
          r =>
            error"Payment creation got unexpected rejection for user ${request.userId}: ${r.toString}." *> R
              .raise[Unit](new Exception(r.toString)),
          _ => info"Payment $id created successfully for user ${request.userId}."
        ))
      } yield id
  }
}
