package su.wps.sweetshop.payments.impl.validators

import cats.data.NonEmptyList

object errors {
  final case class IncorrectParam(key: String, message: String)

  final case class ValidationFailed(errors: NonEmptyList[IncorrectParam]) extends Throwable
}
