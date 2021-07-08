package su.wps.sweetshop.auth.impl.gateways

import su.wps.sweetshop.auth.impl.models.TemplateVar

trait SMSGateway[F[_]] {
  def send(to: String, template: String, variables: List[TemplateVar]): F[Unit]
}
