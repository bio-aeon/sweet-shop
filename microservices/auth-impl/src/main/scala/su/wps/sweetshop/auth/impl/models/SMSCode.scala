package su.wps.sweetshop.auth.impl.models

import java.time.ZonedDateTime

final case class SMSCode(phone: String, code: String, createdAt: ZonedDateTime)
