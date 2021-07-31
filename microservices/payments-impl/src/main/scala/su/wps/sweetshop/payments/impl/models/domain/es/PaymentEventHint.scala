package su.wps.sweetshop.payments.impl.models.domain.es

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

sealed trait PaymentEventHint extends EnumEntry

object PaymentEventHint extends Enum[PaymentEventHint] {
  case object AA extends PaymentEventHint
  case object AB extends PaymentEventHint
  case object AC extends PaymentEventHint
  case object AD extends PaymentEventHint
  case object AE extends PaymentEventHint

  def values: immutable.IndexedSeq[PaymentEventHint] = findValues
}
