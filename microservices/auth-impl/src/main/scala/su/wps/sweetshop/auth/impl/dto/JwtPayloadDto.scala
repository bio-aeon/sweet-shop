package su.wps.sweetshop.auth.impl.dto

import java.time.ZonedDateTime

import io.circe.generic.JsonCodec
import io.circe.java8.time._
import su.wps.sweetshop.auth.impl.models.UserId
import su.wps.sweetshop.auth.impl.utils.CirceSupport._

@JsonCodec
case class JwtPayloadDto(userId: UserId, createdAt: ZonedDateTime)
