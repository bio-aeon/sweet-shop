package su.wps.sweetshop.auth.impl.endpoints

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import cats.~>
import org.http4s.util.CaseInsensitiveString
import org.http4s.{HttpRoutes, Request}
import su.wps.sweetshop.auth.impl.data.AppContext
import tofu.WithRun

import java.util.UUID

object ContextSetter {
  private val TraceIdHeader: String = "X-Request-ID"

  def create[I[_]: Sync, F[_]](
    implicit WR: WithRun[F, I, AppContext]
  ): HttpRoutes[F] => HttpRoutes[I] = { (routes: HttpRoutes[F]) =>
    Kleisli { req: Request[I] =>
      for {
        traceId <- OptionT
          .fromOption(
            req.headers
              .get(CaseInsensitiveString(TraceIdHeader))
              .map(_.value)
          )
          .orElseF(Sync[I].delay(Some(UUID.randomUUID().toString)))
        resp <- OptionT(applyCtx(traceId, routes.run(req.mapK(WR.liftF)).value))
          .map(_.mapK(λ[F ~> I](applyCtx(traceId, _))))
      } yield resp
    }
  }

  private[endpoints] def applyCtx[I[_], F[_], A](traceId: String, fa: => F[A])(
    implicit WR: WithRun[F, I, AppContext]
  ): I[A] =
    WR.runContext(fa)(AppContext(traceId))
}
