package su.wps.sweetshop.auth.impl.gateways

import cats.effect.Sync
import cats.syntax.functor._
import com.plivo.api.Plivo
import com.plivo.api.models.message.Message
import su.wps.sweetshop.auth.impl.config.PlivoConfig
import su.wps.sweetshop.auth.impl.models.TemplateVar

import java.util.Collections

object PlivoGateway {

  def create[I[_], F[_]: Sync](conf: PlivoConfig)(implicit I: Sync[I]): I[SMSGateway[F]] =
    I.delay(Plivo.init(conf.authId, conf.authToken)).as(new Impl[F](conf))

  private final class Impl[F[_]](conf: PlivoConfig)(implicit F: Sync[F]) extends SMSGateway[F] {
    def send(to: String, template: String, variables: List[TemplateVar]): F[Unit] = {
      val content = variables.foldLeft(template) { (acc, mergeVar) =>
        acc.replaceAll(s"""\\{\\{${mergeVar.name}\\}\\}\\w*""", mergeVar.content)
      }

      F.delay(
        Message
          .creator(conf.sourcePhone, Collections.singletonList(to), content)
          .create()
      )
    }
  }
}
