package su.wps.sweetshop.auth.impl.endpoints

import cats.effect.{Concurrent, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import io.circe.Encoder
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.Logger
import org.http4s.{EntityDecoder, HttpRoutes, Request}
import su.wps.sweetshop.auth.api.models.{
  CreateSMSCodeRequest,
  CreateTokenBySMSCodeRequest,
  TokenResult
}
import su.wps.sweetshop.auth.impl.data.AppContext
import su.wps.sweetshop.auth.impl.models.implicits.requests._
import su.wps.sweetshop.auth.impl.models.implicits.results._
import su.wps.sweetshop.auth.impl.wirings.ServiceWirings
import tofu.WithRun
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._

trait Routes[I[_]] {
  val routes: HttpRoutes[I]
}

object Routes {

  def create[I[_]: Sync, F[_]: Concurrent](
    serviceWirings: ServiceWirings[F]
  )(implicit WR: WithRun[F, I, AppContext], logs: Logs[I, F]): I[Routes[I]] =
    logs.forService[Routes[I]].map { implicit logging =>
      val contextSetter: HttpRoutes[F] => HttpRoutes[I] = ContextSetter.create[I, F]
      new Impl[I, F](contextSetter, serviceWirings)
    }

  private final class Impl[I[_], F[_]: Concurrent: Logging](
    contextSetter: HttpRoutes[F] => HttpRoutes[I],
    serviceWirings: ServiceWirings[F]
  ) extends Routes[I]
      with Http4sDsl[F] {

    import serviceWirings._

    private val apiRoot: / = Root / "api"

    private val apiRoutes = HttpRoutes.of[F] {
      case r @ POST -> apiRoot / "sms-codes" =>
        processRequest[CreateSMSCodeRequest, Unit](r, r => smsCodeService.createSMSCode(r.phone))
      case r @ POST -> apiRoot / "sms-code-tokens" =>
        processRequest[CreateTokenBySMSCodeRequest, TokenResult](
          r,
          r => authService.createTokenBySMSCode(r.phone, r.code)
        )
    }

    val routes: HttpRoutes[I] = {
      contextSetter {
        Logger.httpRoutes(logHeaders = true, logBody = true, logAction = (log _).some)(apiRoutes)
      }
    }

    private def log(msg: String): F[Unit] =
      info"$msg"

    private def processRequest[Req: EntityDecoder[F, *], Resp: Encoder](request: Request[F],
                                                                        handler: Req => F[Resp]) =
      request.as[Req].flatMap(handler).map(_.asJson).flatMap(Ok(_))
  }
}
