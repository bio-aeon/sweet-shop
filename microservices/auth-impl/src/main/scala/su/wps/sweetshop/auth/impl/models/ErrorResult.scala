package su.wps.sweetshop.auth.impl.models

import cats.data.NonEmptyList
import io.circe.generic.JsonCodec
import su.wps.sweetshop.auth.impl.models

@JsonCodec
sealed trait ErrorResult

object ErrorResult {
  case class ValidationFailed(errors: NonEmptyList[models.Error]) extends ErrorResult
  case class BusinessLogicError(message: String) extends ErrorResult
  case class InternalError(message: String) extends ErrorResult
  case object NotFound extends ErrorResult
  case object Forbidden extends ErrorResult
}
