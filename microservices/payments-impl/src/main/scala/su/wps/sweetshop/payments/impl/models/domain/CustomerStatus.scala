package su.wps.sweetshop.payments.impl.models.domain

import doobie.Meta
import enumeratum.values._

sealed abstract class CustomerStatus(val value: String) extends StringEnumEntry

object CustomerStatus extends StringEnum[CustomerStatus] {
  case object New extends CustomerStatus("NEW")
  case object Registered extends CustomerStatus("REGISTERED")

  val values = findValues

  implicit val meta: Meta[CustomerStatus] = Meta[String].timap(CustomerStatus.withValue)(_.value)
}
