package su.wps.sweetshop.payments.impl.models.domain.es

import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}

sealed trait CustomerEvent extends Product with Serializable

final case class CustomerCreated(email: String, password: String) extends CustomerEvent

object CustomerCreated {
  implicit val encoder: Encoder[CustomerCreated] = deriveEncoder
  val decoder: Decoder[CustomerEvent] = deriveDecoder[CustomerCreated].tryDecode
}

case object CustomerRegistered extends CustomerEvent {
  implicit val encoder: Encoder[CustomerRegistered.type] = Encoder.encodeUnit.contramap(_ => ())
  val decoder: Decoder[CustomerEvent] = Decoder.const(CustomerRegistered).tryDecode
}

object CustomerEvent {
  implicit val encode: Encoder[CustomerEvent] = {
    case x: CustomerCreated => x.asJson
    case CustomerRegistered => ().asJson
  }

  implicit val eventsourcedEvent: EventsourcedEvent[CustomerEvent] =
    new EventsourcedEvent[CustomerEvent] {
      type Hint = CustomerEventHint

      implicit val hintDecoder: Decoder[CustomerEventHint] =
        Decoder[String].map(CustomerEventHint.withName)

      import CustomerEventHint._

      def decoderByHint(hint: this.Hint): Decoder[CustomerEvent] =
        hint match {
          case AA => CustomerCreated.decoder
          case AB => CustomerRegistered.decoder
        }

      def hintByEvent(event: CustomerEvent): Hint =
        event match {
          case _: CustomerCreated => AA
          case CustomerRegistered => AB
        }
    }
}
