package su.wps.sweetshop.payments.impl.endpoints

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated}
import cats.effect.{Concurrent, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import io.circe.Encoder
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.Logger
import org.http4s.{EntityDecoder, HttpRoutes, Request, Response}
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
import su.wps.sweetshop.payments.impl.validators.Validator
import su.wps.sweetshop.payments.impl.validators.errors.{IncorrectParam, ValidationFailed}
import su.wps.sweetshop.payments.impl.wirings.{ServiceWirings, ValidatorWirings}
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._
import tofu.{Raise, WithRun}

trait Routes[I[_]] {
  val routes: HttpRoutes[I]
}

object Routes {

  def create[I[_]: Sync, F[_]: Concurrent](
    paytureConfig: PaytureConfig,
    serviceWirings: ServiceWirings[F],
    validatorWirings: ValidatorWirings[F]
  )(implicit WR: WithRun[F, I, AppContext], logs: Logs[I, F]): I[Routes[I]] =
    logs.forService[Routes[I]].map { implicit logging =>
      val contextSetter: HttpRoutes[F] => HttpRoutes[I] = ContextSetter.create[I, F]
      new Impl[I, F](contextSetter, paytureConfig, serviceWirings, validatorWirings)
    }

  private final class Impl[I[_], F[_]: Concurrent: Logging](
    contextSetter: HttpRoutes[F] => HttpRoutes[I],
    paytureConfig: PaytureConfig,
    serviceWirings: ServiceWirings[F],
    validatorWirings: ValidatorWirings[F]
  )(implicit V: Raise[F, ValidationFailed])
      extends Routes[I]
      with Http4sDsl[F] {

    import serviceWirings._
    import validatorWirings._

    private val apiRoot: / = Root / "api"

    private val apiRoutes = HttpRoutes.of[F] {
      case r @ POST -> apiRoot / "card-links" =>
        processValidatedRequest[InitCardLinkRequest, InitCardLinkRequest, InitCardLinkResult](
          r,
          initCardLinkValidator,
          cardLinkService
            .initCardLink(_)
            .map(x => InitCardLinkResult(s"${paytureConfig.endpoint}/Add", Map("SessionId" -> x)))
        )
      case r @ POST -> apiRoot / "notifications" =>
        r.as[NotificationRequest] >>= { request =>
          paytureNotificationService.handle(request.data) >>= (_ => Ok())
        }
      case r @ POST -> apiRoot / "payments" =>
        processValidatedRequest[CreatePaymentRequest, CreatePaymentRequest, String](
          r,
          createPaymentValidator,
          paymentService.createPayment
        )
      case GET -> apiRoot / "card-links" / "user" / IntVar(userId) / "existence" =>
        cardLinkService.userHasCardLinks(userId).ifM(Ok(), NotFound())
    }

    val validationErrorHandler: HttpErrorHandler[F, ValidationFailed] =
      ValidationHttpErrorHandler[F]

    val routes: HttpRoutes[I] = {
      contextSetter {
        Logger.httpRoutes(logHeaders = true, logBody = true, logAction = (log _).some)(
          validationErrorHandler.handle(apiRoutes)
        )
      }
    }

    private[endpoints] def log(msg: String): F[Unit] =
      info"$msg"

    private[endpoints] def processValidatedRequest[Req: EntityDecoder[F, *], VReq, Resp: Encoder](
      request: Request[F],
      validator: Validator[Validated[NonEmptyList[IncorrectParam], *], F, Req, VReq],
      handler: VReq => F[Resp]
    ) =
      for {
        req <- request.as[Req]
        res <- validator.validate(req) >>= {
          case Valid(x) => handler(x).map(_.asJson).flatMap(Ok(_))
          case Invalid(e) => V.raise[Response[F]](ValidationFailed(e))
        }
      } yield res
  }
}
