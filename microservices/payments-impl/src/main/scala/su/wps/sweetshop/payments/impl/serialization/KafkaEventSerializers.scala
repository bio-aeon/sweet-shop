package su.wps.sweetshop.payments.impl.serialization

import aecor.data.{Enriched, EntityEvent}
import cats.effect._
import cats.syntax.either._
import fs2.kafka._
import io.circe.generic.semiauto._
import io.circe.parser.decode
import io.circe.{Decoder, DecodingFailure, Error}
import su.wps.sweetshop.payments.impl.models.domain.es.{
  EventMetadata,
  EventsourcedEvent,
  JournalEvent
}

import java.nio.charset.StandardCharsets
import java.util.Base64
import scala.util.Try

object KafkaEventSerializers {
  implicit def enrichedDecoder[M: Decoder, E: Decoder]: Decoder[Enriched[M, E]] = deriveDecoder

  implicit def entityDecoder[K: Decoder, A: EventsourcedEvent](
    implicit es: EventsourcedEvent[A]
  ): Decoder[JournalEvent[K, A]] = { c =>
    val payload = c.downField("payload").downField("after")
    for {
      key <- payload.downField("key").as[K]
      seqNr <- payload.downField("seq_nr").as[Long]
      entityRaw <- payload.downField("bytes").as[String]
      implicit0(hintDecoder: Decoder[es.Hint]) = es.hintDecoder
      typeHint <- payload.downField("type_hint").as[es.Hint]
      entityBytes = Base64.getDecoder.decode(entityRaw.getBytes(StandardCharsets.UTF_8))
      implicit0(entityDecoder: Decoder[A]) = es.decoderByHint(typeHint)
      entity <- Try(new String(entityBytes)).toEither
        .flatMap(decode[Enriched[EventMetadata, A]])
        .leftMap(e => DecodingFailure.fromThrowable(e, List.empty))
    } yield EntityEvent(key, seqNr, entity)
  }

  implicit def recordDeserializer[F[_]: Sync, A: Decoder]: RecordDeserializer[F, Either[Error, A]] =
    RecordDeserializer.lift(Deserializer[F, String].map(decode[A](_)))
}
