package su.wps.sweetshop.payments.impl.models.domain.views

import su.wps.sweetshop.payments.impl.models.CardLinkId
import su.wps.sweetshop.payments.impl.models.domain.CardLinkStatus

import java.time.ZonedDateTime

final case class CardLinkView(id: CardLinkId,
                              userId: Int,
                              extCardId: Option[String],
                              maskedPan: Option[String],
                              status: CardLinkStatus,
                              createdAt: ZonedDateTime,
                              version: Long)
