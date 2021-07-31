package su.wps.sweetshop.payments.impl.models.domain.es

import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}

sealed trait CardLinkEvent extends Product with Serializable

final case class CardLinkCreated(userId: Int) extends CardLinkEvent

object CardLinkCreated {
  implicit val encoder: Encoder[CardLinkCreated] = deriveEncoder
  val decoder: Decoder[CardLinkEvent] = deriveDecoder[CardLinkCreated].tryDecode
}

final case class CardLinkActivated(maskedPan: String,
                                   extCardId: String,
                                   cardHolder: String,
                                   expDate: String,
                                   linkedAt: String)
    extends CardLinkEvent

object CardLinkActivated {
  implicit val encoder: Encoder[CardLinkActivated] = deriveEncoder
  val decoder: Decoder[CardLinkEvent] = deriveDecoder[CardLinkActivated].tryDecode
}

object CardLinkEvent {
  implicit val encode: Encoder[CardLinkEvent] = {
    case x: CardLinkCreated => x.asJson
    case x: CardLinkActivated => x.asJson
  }

  implicit val eventsourcedEvent: EventsourcedEvent[CardLinkEvent] =
    new EventsourcedEvent[CardLinkEvent] {
      type Hint = CardLinkEventHint

      implicit val hintDecoder: Decoder[CardLinkEventHint] =
        Decoder[String].map(CardLinkEventHint.withName)

      import CardLinkEventHint._

      def decoderByHint(hint: this.Hint): Decoder[CardLinkEvent] =
        hint match {
          case AA => CardLinkCreated.decoder
          case AB => CardLinkActivated.decoder
        }

      def hintByEvent(event: CardLinkEvent): Hint =
        event match {
          case _: CardLinkCreated => AA
          case _: CardLinkActivated => AB
        }
    }
}
