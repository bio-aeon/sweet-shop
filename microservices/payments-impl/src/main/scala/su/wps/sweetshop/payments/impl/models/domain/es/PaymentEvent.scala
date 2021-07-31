package su.wps.sweetshop.payments.impl.models.domain.es

import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

sealed trait PaymentEvent extends Product with Serializable

final case class PaymentCreated(userId: Int, amount: Int, details: Json) extends PaymentEvent

object PaymentCreated {
  implicit val encoder: Encoder[PaymentCreated] = deriveEncoder
  val decoder: Decoder[PaymentEvent] = deriveDecoder[PaymentCreated].tryDecode
}

case object PaymentAuthorized extends PaymentEvent {
  implicit val encoder: Encoder[PaymentAuthorized.type] = Encoder.encodeUnit.contramap(_ => ())
  val decoder: Decoder[PaymentEvent] = Decoder.const(PaymentAuthorized).tryDecode
}

case object PaymentPreCharged extends PaymentEvent {
  implicit val encoder: Encoder[PaymentPreCharged.type] = Encoder.encodeUnit.contramap(_ => ())
  val decoder: Decoder[PaymentEvent] = Decoder.const(PaymentPreCharged).tryDecode
}

case object PaymentCharged extends PaymentEvent {
  implicit val encoder: Encoder[PaymentCharged.type] = Encoder.encodeUnit.contramap(_ => ())
  val decoder: Decoder[PaymentEvent] = Decoder.const(PaymentCharged).tryDecode
}

case object PaymentFailed extends PaymentEvent {
  implicit val encoder: Encoder[PaymentFailed.type] = Encoder.encodeUnit.contramap(_ => ())
  val decoder: Decoder[PaymentEvent] = Decoder.const(PaymentFailed).tryDecode
}

object PaymentEvent {
  implicit val encode: Encoder[PaymentEvent] = {
    case x: PaymentCreated => x.asJson
    case PaymentAuthorized => ().asJson
    case PaymentPreCharged => ().asJson
    case PaymentCharged => ().asJson
    case PaymentFailed => ().asJson
  }

  implicit val eventsourcedEvent: EventsourcedEvent[PaymentEvent] =
    new EventsourcedEvent[PaymentEvent] {
      type Hint = PaymentEventHint

      implicit val hintDecoder: Decoder[PaymentEventHint] =
        Decoder[String].map(PaymentEventHint.withName)

      import PaymentEventHint._

      def decoderByHint(hint: this.Hint): Decoder[PaymentEvent] =
        hint match {
          case AA => PaymentCreated.decoder
          case AB => PaymentAuthorized.decoder
          case AC => PaymentPreCharged.decoder
          case AD => PaymentCharged.decoder
          case AE => PaymentFailed.decoder
        }

      def hintByEvent(event: PaymentEvent): Hint =
        event match {
          case _: PaymentCreated => AA
          case PaymentAuthorized => AB
          case PaymentPreCharged => AC
          case PaymentCharged => AD
          case PaymentFailed => AE
        }
    }
}
