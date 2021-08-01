package su.wps.sweetshop.payments.impl.endpoints

import cats.effect.{Concurrent, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.Logger
import su.wps.sweetshop.payments.api.models.{
  CreatePaymentRequest,
  InitCardLinkRequest,
  InitCardLinkResult,
  NotificationRequest
}
import su.wps.sweetshop.payments.impl.config.PaytureConfig
import su.wps.sweetshop.payments.impl.data.AppContext
import su.wps.sweetshop.payments.impl.models.implicits.requests._
import su.wps.sweetshop.payments.impl.models.implicits.results._
import su.wps.sweetshop.payments.impl.wirings.ServiceWirings
import tofu.WithRun
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._

trait Routes[I[_]] {
  val routes: HttpRoutes[I]
}

object Routes {

  def create[I[_]: Sync, F[_]: Concurrent](
    paytureConfig: PaytureConfig,
    serviceWirings: ServiceWirings[F]
  )(implicit WR: WithRun[F, I, AppContext], logs: Logs[I, F]): I[Routes[I]] =
    logs.forService[Routes[I]].map { implicit logging =>
      val contextSetter: HttpRoutes[F] => HttpRoutes[I] = ContextSetter.create[I, F]
      new Impl[I, F](contextSetter, paytureConfig, serviceWirings)
    }

  private final class Impl[I[_], F[_]: Concurrent: Logging](
    contextSetter: HttpRoutes[F] => HttpRoutes[I],
    paytureConfig: PaytureConfig,
    serviceWirings: ServiceWirings[F]
  ) extends Routes[I]
      with Http4sDsl[F] {

    import serviceWirings._

    private val apiRoot: / = Root / "api"

    private val apiRoutes = HttpRoutes.of[F] {
      case r @ POST -> apiRoot / "card-links" =>
        r.as[InitCardLinkRequest] >>= { request =>
          cardLinkService.initCardLink(request) >>= { res =>
            Ok(InitCardLinkResult(s"${paytureConfig.endpoint}/Add", Map("SessionId" -> res)).asJson)
          }
        }
      case r @ POST -> apiRoot / "notifications" =>
        r.as[NotificationRequest] >>= { request =>
          paytureNotificationService.handle(request.data) >>= (_ => Ok())
        }
      case r @ POST -> apiRoot / "payments" =>
        r.as[CreatePaymentRequest] >>= { request =>
          paymentService.createPayment(request).map(_.asJson) >>= (Ok(_))
        }
      case GET -> apiRoot / "card-links" / "user" / IntVar(userId) / "existence" =>
        cardLinkService.userHasCardLinks(userId).ifM(Ok(), NotFound())
    }

    val routes: HttpRoutes[I] = {
      contextSetter {
        Logger.httpRoutes(logHeaders = true, logBody = true, logAction = (log _).some)(apiRoutes)
      }
    }

    private def log(msg: String): F[Unit] =
      info"$msg"
  }
}
