package su.wps.sweetshop.payments.api.models

final case class InitCardLinkRequest(userId: Int, ip: String, template: String, returnUrl: String)
