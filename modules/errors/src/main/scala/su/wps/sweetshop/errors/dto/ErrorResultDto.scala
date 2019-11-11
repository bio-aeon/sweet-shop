package su.wps.sweetshop.errors.dto

import cats.data.NonEmptyList

sealed trait ErrorResultDto

object ErrorResultDto {
  case class Error(key: String, message: String)

  case class ValidationFailed(errors: NonEmptyList[Error]) extends ErrorResultDto
  case class BusinessLogicError(message: String) extends ErrorResultDto
  case class InternalError(message: String) extends ErrorResultDto
  case object NotFound extends ErrorResultDto
  case object Forbidden extends ErrorResultDto
  object Unauthenticated extends ErrorResultDto
}
