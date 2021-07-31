package su.wps.sweetshop.payments.impl.models.domain.es

import io.circe.Decoder
import io.circe.generic.semiauto._

import java.time.ZonedDateTime

case class EventMetadata(dt: ZonedDateTime) extends AnyVal

object EventMetadata {
  implicit val decoder: Decoder[EventMetadata] = deriveDecoder
}
