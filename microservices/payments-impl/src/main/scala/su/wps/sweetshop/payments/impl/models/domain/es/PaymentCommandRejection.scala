package su.wps.sweetshop.payments.impl.models.domain.es

sealed trait PaymentCommandRejection

case object PaymentNotFound extends PaymentCommandRejection

case object PaymentIsNotAuthorized extends PaymentCommandRejection

case object PaymentIsAlreadyPreCharged extends PaymentCommandRejection
