package su.wps.sweetshop.payments.impl.validators

import cats.Applicative
import cats.data.{Nested, NonEmptyList}
import cats.syntax.applicative._
import cats.syntax.functor._
import cats.syntax.nested._
import su.wps.sweetshop.payments.api.models.CreatePaymentRequest
import su.wps.sweetshop.payments.impl.validators.errors.IncorrectParam
import tofu.Raise

object CreatePaymentValidator {
  def create[F[_]: Applicative: Raise[*[_], NonEmptyList[IncorrectParam]], G[_]: Applicative]
    : Validator[F, G, CreatePaymentRequest, CreatePaymentRequest] =
    new Impl[F, G]

  private final class Impl[F[_], G[_]](implicit val F: Applicative[F],
                                       val R: Raise[F, NonEmptyList[IncorrectParam]],
                                       val G: Applicative[G])
      extends Validator[F, G, CreatePaymentRequest, CreatePaymentRequest] {

    def validate(input: CreatePaymentRequest): G[F[CreatePaymentRequest]] =
      validateAmount(input.amount).as(input).value

    def validateAmount(amount: Int): Nested[G, F, Int] =
      validate(amount, "amount", "Value must be equal or less than 10000000.")(
        (amount <= 10000000).pure[G]
      ).nested
  }
}
