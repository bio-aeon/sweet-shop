package su.wps.sweetshop.auth.impl.gateways

import java.util.Collections

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import com.plivo.api.Plivo
import com.plivo.api.models.message.Message
import su.wps.sweetshop.auth.impl.config.PlivoConfig
import su.wps.sweetshop.auth.impl.models.TemplateVar

import scala.util.Try

class PlivoGateway[F[_]: Applicative](conf: PlivoConfig) extends SMSGateway[F] {
  Plivo.init(conf.authId, conf.authToken)

  def send(to: String, template: String, variables: List[TemplateVar]): F[Either[String, Unit]] = {
    val content = variables.foldLeft(template) { (acc, mergeVar) =>
      acc.replaceAll(s"""\\{\\{${mergeVar.name}\\}\\}\\w*""", mergeVar.content)
    }

    Try {
      Message
        .creator("+7(342)206-05-64", Collections.singletonList(to), content)
        .create()
    }.toEither.bimap(_.getMessage, _ => ()).pure[F]
  }
}
