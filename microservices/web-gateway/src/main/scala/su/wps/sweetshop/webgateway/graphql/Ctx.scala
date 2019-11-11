package su.wps.sweetshop.webgateway.graphql

import su.wps.sweetshop.auth.api.dto.UserDto

import scala.concurrent.Future

class Ctx {
  def me =
    Future.successful(UserDto(1, "12345"))
}
