package su.wps.sweetshop.payments.impl.models.domain.es

import aecor.data.Folded
import aecor.data.Folded.syntax._
import su.wps.sweetshop.payments.impl.models.domain.CardLinkStatus

final case class CardLinkState(userId: Int,
                               status: CardLinkStatus,
                               maskedPan: Option[String] = None,
                               extCardId: Option[String] = None,
                               cardHolder: Option[String] = None,
                               expDate: Option[String] = None,
                               linkedAt: Option[String] = None) {
  def applyEvent(event: CardLinkEvent): Folded[CardLinkState] = event match {
    case _: CardLinkCreated => impossible
    case e: CardLinkActivated =>
      copy(
        status = CardLinkStatus.Active,
        maskedPan = Some(e.maskedPan),
        extCardId = Some(e.extCardId),
        cardHolder = Some(e.cardHolder),
        expDate = Some(e.expDate),
        linkedAt = Some(e.linkedAt)
      ).next
  }
}

object CardLinkState {
  def fromEvent(event: CardLinkEvent): Folded[CardLinkState] = event match {
    case e: CardLinkCreated =>
      CardLinkState(e.userId, CardLinkStatus.New).next
    case _ => impossible
  }
}
