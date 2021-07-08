package su.wps.sweetshop.auth.impl.models

import cats.effect.Sync
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import su.wps.sweetshop.auth.api.models.{
  CreateSMSCodeRequest,
  CreateTokenBySMSCodeRequest,
  JwtPayload,
  TokenResult
}

object implicits {
  object requests {
    implicit val createSMSCodeRequestDecoder: Decoder[CreateSMSCodeRequest] = deriveDecoder
    implicit def createSMSCodeRequestEntityDecoder[F[_]: Sync]
      : EntityDecoder[F, CreateSMSCodeRequest] =
      jsonOf[F, CreateSMSCodeRequest]

    implicit val createTokenBySMSCodeRequestDecoder: Decoder[CreateTokenBySMSCodeRequest] =
      deriveDecoder
    implicit def createTokenBySMSCodeRequestEntityDecoder[F[_]: Sync]
      : EntityDecoder[F, CreateTokenBySMSCodeRequest] =
      jsonOf[F, CreateTokenBySMSCodeRequest](implicitly, deriveDecoder[CreateTokenBySMSCodeRequest])
  }

  object results {
    implicit val jwtPayloadEncoder: Encoder[JwtPayload] = deriveEncoder

    implicit val tokenResultEncoder: Encoder[TokenResult] = deriveEncoder
  }
}
