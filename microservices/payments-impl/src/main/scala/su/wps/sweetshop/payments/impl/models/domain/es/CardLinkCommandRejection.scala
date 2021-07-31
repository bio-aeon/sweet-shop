package su.wps.sweetshop.payments.impl.models.domain.es

sealed trait CardLinkCommandRejection

case object CardLinkNotFound extends CardLinkCommandRejection
