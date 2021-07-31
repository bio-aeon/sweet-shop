package su.wps.sweetshop.payments.impl.models.domain.es

sealed trait CustomerCommandRejection

case object CustomerNotFound extends CustomerCommandRejection
