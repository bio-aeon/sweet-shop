package su.wps.sweetshop.payments.impl.endpoints

import cats.effect.{Concurrent, Sync}
import cats.syntax.functor._
import cats.syntax.option._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.Logger
import su.wps.sweetshop.payments.impl.data.AppContext
import tofu.WithRun
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._

trait Routes[I[_]] {
  val routes: HttpRoutes[I]
}

object Routes {

  def create[I[_]: Sync, F[_]: Concurrent](implicit WR: WithRun[F, I, AppContext],
                                           logs: Logs[I, F]): I[Routes[I]] =
    logs.forService[Routes[I]].map { implicit logging =>
      val contextSetter: HttpRoutes[F] => HttpRoutes[I] = ContextSetter.create[I, F]
      new Impl[I, F](contextSetter)
    }

  private final class Impl[I[_], F[_]: Concurrent: Logging](
    contextSetter: HttpRoutes[F] => HttpRoutes[I]
  ) extends Routes[I]
      with Http4sDsl[F] {
    private val apiRoot: / = Root / "api"

    private val apiRoutes = HttpRoutes.of[F] {
      case r @ POST -> apiRoot / "card-links" =>
        Ok("ok")
      case r @ POST -> apiRoot / "notifications" =>
        Ok("ok")
      case r @ POST -> apiRoot / "payments" =>
        Ok("ok")
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
