package su.wps.sweetshop.payments.impl.wirings

import cats.Applicative
import cats.data.{NonEmptyList, Validated}
import su.wps.sweetshop.payments.api.models.{CreatePaymentRequest, InitCardLinkRequest}
import su.wps.sweetshop.payments.impl.validators.errors.IncorrectParam
import su.wps.sweetshop.payments.impl.validators.{
  CreatePaymentValidator,
  InitCardLinkValidator,
  Validator
}

final case class ValidatorWirings[F[_]](
  initCardLinkValidator: Validator[Validated[NonEmptyList[IncorrectParam], *],
                                   F,
                                   InitCardLinkRequest,
                                   InitCardLinkRequest],
  createPaymentValidator: Validator[Validated[NonEmptyList[IncorrectParam], *],
                                    F,
                                    CreatePaymentRequest,
                                    CreatePaymentRequest]
)

object ValidatorWirings {

  def create[F[_]: Applicative]: ValidatorWirings[F] = {
    val initCardLinkValidator =
      InitCardLinkValidator.create[Validated[NonEmptyList[IncorrectParam], *], F]
    val createPaymentValidator =
      CreatePaymentValidator.create[Validated[NonEmptyList[IncorrectParam], *], F]
    ValidatorWirings(initCardLinkValidator, createPaymentValidator)
  }
}
