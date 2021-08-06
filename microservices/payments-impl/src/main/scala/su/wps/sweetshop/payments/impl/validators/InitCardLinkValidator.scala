package su.wps.sweetshop.payments.impl.validators

import cats.Applicative
import cats.data.{Nested, NonEmptyList}
import cats.syntax.applicative._
import cats.syntax.apply._
import cats.syntax.functor._
import cats.syntax.nested._
import su.wps.sweetshop.payments.api.models.InitCardLinkRequest
import su.wps.sweetshop.payments.impl.validators.errors.IncorrectParam
import tofu.Raise

object InitCardLinkValidator {
  def create[F[_]: Applicative: Raise[*[_], NonEmptyList[IncorrectParam]], G[_]: Applicative]
    : Validator[F, G, InitCardLinkRequest, InitCardLinkRequest] =
    new Impl[F, G]

  private final class Impl[F[_], G[_]](implicit val F: Applicative[F],
                                       val R: Raise[F, NonEmptyList[IncorrectParam]],
                                       val G: Applicative[G])
      extends Validator[F, G, InitCardLinkRequest, InitCardLinkRequest] {

    def validate(input: InitCardLinkRequest): G[F[InitCardLinkRequest]] =
      (validateIp(input.ip) <* validateTemplate(input.template) <* validateReturnUrl(
        input.returnUrl
      )).as(input).value

    private def validateIp(ip: String): Nested[G, F, String] =
      validate(ip, "ip", "Incorrect value.")(ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$").pure[G]).nested

    private def validateTemplate(template: String): Nested[G, F, String] =
      validateLimited(template, "template", 5, 255)

    private def validateReturnUrl(returnUrl: String): Nested[G, F, String] =
      validateLimited(returnUrl, "returnUrl", 5, 255)
  }
}
