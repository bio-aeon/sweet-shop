package su.wps.sweetshop.payments.impl.models.domain.es

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

sealed trait CustomerEventHint extends EnumEntry

object CustomerEventHint extends Enum[CustomerEventHint] {
  case object AA extends CustomerEventHint
  case object AB extends CustomerEventHint

  def values: immutable.IndexedSeq[CustomerEventHint] = findValues
}
