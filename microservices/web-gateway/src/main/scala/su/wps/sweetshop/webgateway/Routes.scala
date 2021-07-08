package su.wps.sweetshop.webgateway

import cats.effect._
import io.circe.Encoder
import io.circe.generic.semiauto._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import su.wps.sweetshop.auth.api.models.UserResult

final class Routes[F[_]: Sync] extends Http4sDsl[F] {

  implicit val userResultEncoder: Encoder[UserResult] = deriveEncoder

  def routes = HttpRoutes.of[F] {
    case r @ GET -> Root / "api" / "v1" / "me" =>
      Ok(UserResult(1, "71111111111").asJson)
  }
}
