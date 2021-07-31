package su.wps.sweetshop.payments.impl.models.domain.es

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

sealed trait CardLinkEventHint extends EnumEntry

object CardLinkEventHint extends Enum[CardLinkEventHint] {
  case object AA extends CardLinkEventHint
  case object AB extends CardLinkEventHint

  def values: immutable.IndexedSeq[CardLinkEventHint] = findValues
}
