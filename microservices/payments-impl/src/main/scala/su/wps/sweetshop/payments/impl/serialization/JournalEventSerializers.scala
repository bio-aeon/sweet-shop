package su.wps.sweetshop.payments.impl.serialization

import aecor.journal.postgres.PostgresEventJournal
import aecor.journal.postgres.PostgresEventJournal.Serializer.TypeHint
import cats.syntax.either._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, jawn}
import su.wps.sweetshop.payments.impl.models.domain.es._

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

object JournalEventSerializers {

  object CardLinkEventSerializer extends PostgresEventJournal.Serializer[Event[CardLinkEvent]] {

    val esEvent: EventsourcedEvent[CardLinkEvent] = EventsourcedEvent[CardLinkEvent]

    def serialize(a: Event[CardLinkEvent]): (TypeHint, Array[Byte]) =
      esEvent.hintByEvent(a.event).asInstanceOf[CardLinkEventHint].entryName -> a.asJson.noSpaces
        .getBytes(StandardCharsets.UTF_8)

    def deserialize(typeHint: TypeHint,
                    bytes: Array[Byte]): Either[Throwable, Event[CardLinkEvent]] = {

      implicit val decoder: Decoder[CardLinkEvent] =
        esEvent.decoderByHint(CardLinkEventHint.withName(typeHint).asInstanceOf[esEvent.Hint])

      decodeBytes[Event[CardLinkEvent]](bytes)
    }
  }

  object CustomerEventSerializer extends PostgresEventJournal.Serializer[Event[CustomerEvent]] {

    val esEvent: EventsourcedEvent[CustomerEvent] = EventsourcedEvent[CustomerEvent]

    def serialize(a: Event[CustomerEvent]): (TypeHint, Array[Byte]) =
      esEvent.hintByEvent(a.event).asInstanceOf[CustomerEventHint].entryName -> a.asJson.noSpaces
        .getBytes(StandardCharsets.UTF_8)

    def deserialize(typeHint: TypeHint,
                    bytes: Array[Byte]): Either[Throwable, Event[CustomerEvent]] = {

      implicit val decoder: Decoder[CustomerEvent] =
        esEvent.decoderByHint(CustomerEventHint.withName(typeHint).asInstanceOf[esEvent.Hint])

      decodeBytes[Event[CustomerEvent]](bytes)
    }
  }

  object PaymentEventSerializer extends PostgresEventJournal.Serializer[Event[PaymentEvent]] {

    val esEvent: EventsourcedEvent[PaymentEvent] = EventsourcedEvent[PaymentEvent]

    def serialize(a: Event[PaymentEvent]): (TypeHint, Array[Byte]) =
      esEvent.hintByEvent(a.event).asInstanceOf[PaymentEventHint].entryName -> a.asJson.noSpaces
        .getBytes(StandardCharsets.UTF_8)

    def deserialize(typeHint: TypeHint,
                    bytes: Array[Byte]): Either[Throwable, Event[PaymentEvent]] = {

      implicit val decoder: Decoder[PaymentEvent] =
        esEvent.decoderByHint(PaymentEventHint.withName(typeHint).asInstanceOf[esEvent.Hint])

      decodeBytes[Event[PaymentEvent]](bytes)
    }
  }

  private[serialization] def decodeBytes[A](bytes: Array[Byte])(implicit D: Decoder[A]) =
    jawn
      .parseByteBuffer(ByteBuffer.wrap(bytes))
      .flatMap(D.decodeJson)
      .leftMap(x => x.fillInStackTrace())
}
