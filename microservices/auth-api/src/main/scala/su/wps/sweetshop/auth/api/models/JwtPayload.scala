package su.wps.sweetshop.auth.api.models

import java.time.ZonedDateTime

final case class JwtPayload(userId: Int, createdAt: ZonedDateTime)
