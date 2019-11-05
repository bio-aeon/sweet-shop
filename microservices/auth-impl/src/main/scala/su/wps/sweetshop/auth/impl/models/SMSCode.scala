package su.wps.sweetshop.auth.impl.models

import java.time.ZonedDateTime

case class SMSCode(phone: String, code: String, createdAt: ZonedDateTime)
