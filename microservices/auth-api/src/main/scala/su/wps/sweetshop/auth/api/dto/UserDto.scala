package su.wps.sweetshop.auth.api.dto

import io.circe.generic.JsonCodec

@JsonCodec
case class UserDto(id: Int, phone: String)
