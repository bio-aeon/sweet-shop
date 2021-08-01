package su.wps.sweetshop.payments.impl.models

import cats.effect.Sync
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import su.wps.sweetshop.auth.api.models.UserCreated
import su.wps.sweetshop.auth.api.models.UserCreated.UserContact
import su.wps.sweetshop.payments.api.models.{
  ChargeRequired,
  CreatePaymentRequest,
  InitCardLinkRequest,
  InitCardLinkResult,
  NotificationRequest
}

object implicits {
  object requests {
    implicit val initCardLinkRequestDecoder: Decoder[InitCardLinkRequest] = deriveDecoder
    implicit def initCardLinkRequestEntityDecoder[F[_]: Sync]
      : EntityDecoder[F, InitCardLinkRequest] =
      jsonOf[F, InitCardLinkRequest]

    implicit val createPaymentRequestDecoder: Decoder[CreatePaymentRequest] = deriveDecoder
    implicit def createPaymentRequestEntityDecoder[F[_]: Sync]
      : EntityDecoder[F, CreatePaymentRequest] =
      jsonOf[F, CreatePaymentRequest]

    implicit val notificationRequestDecoder: Decoder[NotificationRequest] = deriveDecoder
    implicit def notificationRequestEntityDecoder[F[_]: Sync]
      : EntityDecoder[F, NotificationRequest] =
      jsonOf[F, NotificationRequest]
  }

  object results {
    implicit val initCardLinkResultEncoder: Encoder[InitCardLinkResult] = deriveEncoder
  }

  object events {
    implicit val userContactDecoder: Decoder[UserContact] = deriveDecoder
    implicit val userCreatedDecoder: Decoder[UserCreated] = deriveDecoder

    implicit val chargeRequiredDecoder: Decoder[ChargeRequired] = deriveDecoder
  }
}
