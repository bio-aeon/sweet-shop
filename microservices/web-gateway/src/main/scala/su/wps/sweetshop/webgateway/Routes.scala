package su.wps.sweetshop.webgateway

import cats.effect._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import su.wps.sweetshop.auth.api.dto.UserDto

final class Routes[F[_]: Sync] extends Http4sDsl[F] {

  def routes = HttpRoutes.of[F] {
    case r @ GET -> Root / "api" / "v1" / "me" =>
      Ok(UserDto(1, "71111111111").asJson)
  }
}
