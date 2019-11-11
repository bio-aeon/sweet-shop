package su.wps.sweetshop.auth.impl.utils

import io.circe.{Decoder, Encoder}
import shapeless._

trait CirceSupport {
  implicit def encoderValueClass[T <: AnyVal, V](implicit
                                                 g: Lazy[Generic.Aux[T, V :: HNil]],
                                                 e: Encoder[V]): Encoder[T] = Encoder.instance {
    value =>
      e(g.value.to(value).head)
  }
  implicit def decoderValueClass[T <: AnyVal, V](implicit
                                                 g: Lazy[Generic.Aux[T, V :: HNil]],
                                                 d: Decoder[V]): Decoder[T] = Decoder.instance {
    cursor =>
      d(cursor).map { value =>
        g.value.from(value :: HNil)
      }
  }
}

object CirceSupport extends CirceSupport