package su.wps.sweetshop.payments.api.models

final case class InitCardLinkResult(url: String, payload: Map[String, String])
