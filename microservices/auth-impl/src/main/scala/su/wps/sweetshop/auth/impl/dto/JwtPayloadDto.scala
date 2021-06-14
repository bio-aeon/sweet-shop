package su.wps.sweetshop.auth.impl.dto

import java.time.ZonedDateTime

import io.circe.generic.JsonCodec
import su.wps.sweetshop.auth.impl.models.UserId
import su.wps.sweetshop.auth.impl.utils.CirceSupport._

@JsonCodec
case class JwtPayloadDto(userId: UserId, createdAt: ZonedDateTime)
