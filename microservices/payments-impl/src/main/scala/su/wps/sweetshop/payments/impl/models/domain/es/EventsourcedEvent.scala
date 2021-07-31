package su.wps.sweetshop.payments.impl.models.domain.es

import io.circe.Decoder

trait EventsourcedEvent[Event] {
  type Hint

  implicit val hintDecoder: Decoder[Hint]

  def decoderByHint(hint: Hint): Decoder[Event]

  def hintByEvent(event: Event): Hint
}

object EventsourcedEvent {
  final def apply[A](implicit instance: EventsourcedEvent[A]): EventsourcedEvent[A] = instance
}
