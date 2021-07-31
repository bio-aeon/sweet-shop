package su.wps.sweetshop.payments.impl.models.domain

import enumeratum.values._

sealed abstract class PaymentStatus(val value: String) extends StringEnumEntry

object PaymentStatus extends StringEnum[PaymentStatus] {
  case object New extends PaymentStatus("NEW")
  case object Authorized extends PaymentStatus("AUTHORIZED")
  case object PreCharged extends PaymentStatus("PRE_CHARGED")
  case object Charged extends PaymentStatus("CHARGED")
  case object Failed extends PaymentStatus("FAILED")

  val values = findValues
}
