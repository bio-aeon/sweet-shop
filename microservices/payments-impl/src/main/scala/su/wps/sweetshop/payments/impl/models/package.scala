package su.wps.sweetshop.payments.impl

import aecor.encoding.{KeyDecoder => AecorKeyDecoder, KeyEncoder => AecorKeyEncoder}
import cats.kernel.Hash
import doobie.Meta
import io.estatico.newtype.macros.newtype

package object models {
  @newtype final case class CardLinkId(value: String)

  object CardLinkId {
    implicit val meta: Meta[CardLinkId] = deriving
    implicit val hash: Hash[CardLinkId] = Hash.fromUniversalHashCode
    implicit val aecorKeyEncoder: AecorKeyEncoder[CardLinkId] = deriving
    implicit val aecorKeyDecoder: AecorKeyDecoder[CardLinkId] = deriving
  }

  @newtype final case class CustomerId(value: Int)

  object CustomerId {
    implicit val meta: Meta[CustomerId] = deriving
    implicit val hash: Hash[CustomerId] = Hash.fromUniversalHashCode
    implicit val aecorKeyEncoder: AecorKeyEncoder[CustomerId] = deriving
    implicit val aecorKeyDecoder: AecorKeyDecoder[CustomerId] = deriving
  }

  @newtype final case class PaymentId(value: String)

  object PaymentId {
    implicit val hash: Hash[PaymentId] = Hash.fromUniversalHashCode
    implicit val aecorKeyEncoder: AecorKeyEncoder[PaymentId] = deriving
    implicit val aecorKeyDecoder: AecorKeyDecoder[PaymentId] = deriving
  }
}
