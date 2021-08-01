package su.wps.sweetshop.payments.api.models

import io.circe.Json

final case class CreatePaymentRequest(userId: Int, amount: Int, details: Json)
