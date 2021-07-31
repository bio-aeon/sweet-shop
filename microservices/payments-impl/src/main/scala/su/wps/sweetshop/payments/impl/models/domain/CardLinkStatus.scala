package su.wps.sweetshop.payments.impl.models.domain

import doobie.Meta
import enumeratum.values._

sealed abstract class CardLinkStatus(val value: String) extends StringEnumEntry

object CardLinkStatus extends StringEnum[CardLinkStatus] {
  case object New extends CardLinkStatus("NEW")
  case object Active extends CardLinkStatus("ACTIVE")

  val values = findValues

  implicit val meta: Meta[CardLinkStatus] = Meta[String].timap(CardLinkStatus.withValue)(_.value)
}
